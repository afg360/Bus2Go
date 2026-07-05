package dev.mainhq.bus2go.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dev.mainhq.bus2go.domain.backgroundtask.DatabaseDownloadScheduler
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.use_case.ObserveDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.db_state.CheckDatabaseUpdateRequired
import dev.mainhq.bus2go.domain.use_case.db_state.SetDatabaseExpirationDate
import dev.mainhq.bus2go.domain.use_case.db_state.SetUpdateDbDialogLastAsToday
import dev.mainhq.bus2go.domain.use_case.db_state.WasUpdateDialogShownToday
import dev.mainhq.bus2go.domain.use_case.settings.CheckIsBus2GoServer
import dev.mainhq.bus2go.domain.use_case.settings.GetSettings
import dev.mainhq.bus2go.presentation.config.ServerType
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.coroutineContext

class MainActivityViewModel(
	private val checkDatabaseUpdateRequired: CheckDatabaseUpdateRequired,
	private val wasUpdateDialogShownToday: WasUpdateDialogShownToday,
	private val setUpdateDbDialogLastAsToday: SetUpdateDbDialogLastAsToday,
	private val setDatabaseExpirationDate: SetDatabaseExpirationDate,
	private val getSettings: GetSettings,
	private val checkIsBus2GoServer: CheckIsBus2GoServer,
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask,
	private val observeDownloadDatabaseTask: ObserveDownloadDatabaseTask
): ViewModel() {

	//TODO clean up this class by using flows correctly
	// checking if dialog has been shown today seems to be broken, need to write tests for that

	private val _activityFragment = MutableStateFlow(ActivityFragment.HOME)
	val activityFragment = _activityFragment.asStateFlow()

	//use a shared flow to have the same value shared among collectors
	private val _resp = flow {
		emit(checkDatabaseUpdateRequired.invoke())
	}.shareIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000))

	private val _wasUpdateDialogShownToday = wasUpdateDialogShownToday.invoke()
		.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

	//TODO instead of storing a string, store some sort of enum (with a string value attached to it)
	val updateTextViewString = _resp.map { resp ->
		when (resp) {
			is Result.Error -> ""
			is Result.Success<List<DbToDownload>> -> {
				//TODO use a string resource
				if (resp.data.isEmpty()) ""
				else if (resp.data.size == 1) "${resp.data.first()} database needs to be updated"
				else "databases need an update...?"
			}
		}
	}.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), "")

	val showUpdateDbDialog = combine(_resp, _wasUpdateDialogShownToday)
	{ resp, wasUpdateDialogShownToday ->
		when (resp) {
			is Result.Error -> wasUpdateDialogShownToday
			is Result.Success<List<DbToDownload>> -> resp.data.isNotEmpty() && !wasUpdateDialogShownToday
		}
	}

	init {
		viewModelScope.launch {
			_wasUpdateDialogShownToday.collect {
				if (!it){
					//must happen after showUpdateDbDialog...?
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
			setDatabaseExpirationDate.invoke(LocalDate.now().plusDays(days.toLong()))
		}
	}

	fun setUpdateDbExpirationDate(unixDay: Long){
		viewModelScope.launch {
			setDatabaseExpirationDate.invoke(LocalDate.ofEpochDay(unixDay))
			//FIXME setup the app theme?
		}
	}

	private val _updateDbState = MutableStateFlow<UpdateDbState>(UpdateDbState.NoShow)
	val updateDbState = _updateDbState.asStateFlow()

	private val _notification = MutableSharedFlow<UiState<String>>(replay = 0)
	val notification = _notification.asSharedFlow()

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
				when(val resp = checkIsBus2GoServer.invoke(server, ServerType.SELF_HOSTED)){
					is Result.Error -> _updateDbState.update { UpdateDbState.NotConnectedToInternet }
					is Result.Success<Boolean> -> {
						if (resp.data) {
							//FIXME only update the one that is actually needed to be updated...
							// to be able to do that, we need to store not only the expiration date, but also
							// to which database this data is part of.... but for now simply download everything
							_resp.collectLatest {
								assert(it is Result.Success)
								it as Result.Success
								scheduleDownloadDatabaseTask.invoke(it.data).forEach { workId ->
									observeDownloadDatabaseTask.invoke(workId).collect { workInfo ->
										when(workInfo.state) {
											WorkInfo.State.SUCCEEDED -> {
												if (workInfo.outputData.getString("SAME")!!.toBoolean()){
													_notification.emit(UiState.Success("Server database not updated yet"))
												}
												//else { }
											}
											WorkInfo.State.FAILED -> TODO()
											WorkInfo.State.BLOCKED -> TODO()
											WorkInfo.State.CANCELLED -> TODO()
											else -> {}
										}
									}
								}
								_updateDbState.update { UpdateDbState.NoShow }
							}
						}
						else {
							//Not a valid bus2go server or the server is not on/does not accept connections...
							_updateDbState.update { UpdateDbState.Error("This is not a valid bus2go") }
						}
					}
				}
			}
		}
	}
}