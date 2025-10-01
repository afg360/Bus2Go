package dev.mainhq.bus2go.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.db_state.CheckDatabaseUpdateRequired
import dev.mainhq.bus2go.domain.use_case.db_state.SetDatabaseExpirationDate
import dev.mainhq.bus2go.domain.use_case.db_state.SetUpdateDbDialogLastAsToday
import dev.mainhq.bus2go.domain.use_case.db_state.WasUpdateDialogShownToday
import dev.mainhq.bus2go.domain.use_case.settings.CheckIsBus2GoServer
import dev.mainhq.bus2go.domain.use_case.settings.GetSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivityViewModel(
	private val checkDatabaseUpdateRequired: CheckDatabaseUpdateRequired,
	private val wasUpdateDialogShownToday: WasUpdateDialogShownToday,
	private val setUpdateDbDialogLastAsToday: SetUpdateDbDialogLastAsToday,
	private val setDatabaseExpirationDate: SetDatabaseExpirationDate,
	private val getSettings: GetSettings,
	private val checkIsBus2GoServer: CheckIsBus2GoServer,
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask
): ViewModel() {

	private val _activityFragment = MutableStateFlow(ActivityFragment.HOME)
	val activityFragment = _activityFragment.asStateFlow()

	//use a shared flow to have the same value shared among collectors
	private val _resp = flow {
		emit(checkDatabaseUpdateRequired.invoke())
	}.shareIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000))

	//use a shared flow to have the same value shared among collectors
	private val _wasUpdateDialogShownToday = flow {
		emit(wasUpdateDialogShownToday.invoke())
	}.shareIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000))

	val showUpdateTextView = _resp.map { resp ->
		when (resp) {
			is Result.Error -> false
			is Result.Success<LocalDate?> -> resp.data == null
		}
	}.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), false)

	val showUpdateDbDialog = combine(_resp, _wasUpdateDialogShownToday)
	{ resp, wasUpdateDialogShownToday ->
		when (resp) {
			is Result.Error -> wasUpdateDialogShownToday
			is Result.Success<LocalDate?> -> resp.data == null && !wasUpdateDialogShownToday
		}
	}

	init {
		viewModelScope.launch {
			_wasUpdateDialogShownToday.collect {
				if (!it){
					setUpdateDbDialogLastAsToday.invoke()
				}
			}
		}
	}

	fun setActivityType(activityFragment: ActivityFragment){
		//prevents spamming and rerendering of the same fragment...
		if (activityFragment != _activityFragment.value)
			_activityFragment.update { activityFragment }
	}

	fun setUpdateDbExpirationDate(days: Int){
		viewModelScope.launch {
			setDatabaseExpirationDate(LocalDate.now().plusDays(days.toLong()))
		}
	}

	fun setUpdateDbExpirationDate(unixDay: Long){
		viewModelScope.launch {
			setDatabaseExpirationDate(LocalDate.ofEpochDay(unixDay))
			//FIXME setup the app theme?
		}
	}

	private val _updateDbState = MutableStateFlow<UpdateDbState>(UpdateDbState.NoShow)
	val updateDbState = _updateDbState.asStateFlow()

	fun updateDatabase(){
		viewModelScope.launch {
			//be sure to have a valid bus2go database -> this in itself should check that we are connected to the internet
			//if we are, start the download process, show some loading in notifs, and make a toast about download that has started
			//else, make a toast error (or perhaps instead use a snackbar so that we can try again)

			val server = getSettings.invoke().serverChoice
			if (server.isBlank()){
				//no server was selected by the user, prompt him to do so or to do that later
				_updateDbState.update { UpdateDbState.Error("Please connect to a bus2go server") }
				//TODO("Not Implemented")
			}
			else {
				when(val resp = checkIsBus2GoServer.invoke(server)){
					is Result.Error -> _updateDbState.update { UpdateDbState.NotConnectedToInternet }
					is Result.Success<Boolean> -> {
						if (resp.data) {
							//FIXME only update the one that is actually needed to be updated...
							// to be able to do that, we need to store not only the expiration date, but also
							// to which database this data is part of.... but for now simply download everything
							scheduleDownloadDatabaseTask.invoke(DbToDownload.ALL)
							_updateDbState.update { UpdateDbState.NoShow }
						}
						else {
							//Not a valid bus2go server or the server is not on/does not accept connections...
							_updateDbState.update { UpdateDbState.Error("This is not a valid bus2go ") }
						}
					}
				}
			}
		}
	}

}