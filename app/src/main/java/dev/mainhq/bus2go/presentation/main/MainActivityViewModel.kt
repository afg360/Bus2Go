package dev.mainhq.bus2go.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.use_case.db_state.CheckDatabaseUpdateRequired
import dev.mainhq.bus2go.domain.use_case.db_state.IsFirstTimeAppLaunched
import dev.mainhq.bus2go.domain.use_case.db_state.SetDatabaseState
import dev.mainhq.bus2go.presentation.utils.ActivityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivityViewModel(
	private val checkDatabaseUpdateRequired: CheckDatabaseUpdateRequired,
	private val setDatabaseState: SetDatabaseState,
): ViewModel() {

	private val _activityType: MutableStateFlow<ActivityType> = MutableStateFlow(ActivityType.HOME)
	val activityType: StateFlow<ActivityType> get() = _activityType

	private val _updateDbState: MutableStateFlow<LocalDate?> = MutableStateFlow(null)
	val updateDbState: StateFlow<LocalDate?> get() = _updateDbState

	private val _showAlert: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val showAlert: StateFlow<Boolean> get() = _showAlert



	fun setActivityType(activityType: ActivityType){
		//prevents spamming and rerendering of the same fragment...
		if (activityType != _activityType.value)
			_activityType.value = activityType
	}

	fun setUpdateDbState(day: Long){
		viewModelScope.launch {
			setDatabaseState(LocalDate.now().plusDays(day))
			//FIXME setup the app theme?
		}
	}

}