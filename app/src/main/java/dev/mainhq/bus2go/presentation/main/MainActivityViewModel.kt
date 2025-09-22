package dev.mainhq.bus2go.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.use_case.db_state.CheckDatabaseUpdateRequired
import dev.mainhq.bus2go.domain.use_case.db_state.SetDatabaseExpirationDate
import dev.mainhq.bus2go.domain.use_case.db_state.SetUpdateDbDialogLastAsToday
import dev.mainhq.bus2go.domain.use_case.db_state.WasUpdateDialogShownToday
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivityViewModel(
	private val checkDatabaseUpdateRequired: CheckDatabaseUpdateRequired,
	private val wasUpdateDialogShownToday: WasUpdateDialogShownToday,
	private val setUpdateDbDialogLastAsToday: SetUpdateDbDialogLastAsToday,
	private val setDatabaseExpirationDate: SetDatabaseExpirationDate,
): ViewModel() {

	private val _activityFragment = MutableStateFlow(ActivityFragment.HOME)
	val activityFragment = _activityFragment.asStateFlow()

	val showUpdateDbDialog = flow {
		when (val resp = checkDatabaseUpdateRequired.invoke()) {
			is Result.Error -> emit(wasUpdateDialogShownToday.invoke())
			is Result.Success<LocalDate?> -> {
				emit(resp.data == null && !wasUpdateDialogShownToday.invoke())
				if (!wasUpdateDialogShownToday.invoke()){
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

}