package dev.mainhq.bus2go.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.use_case.db_state.CheckDatabaseUpdateRequired
import dev.mainhq.bus2go.domain.use_case.db_state.SetDatabaseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivityViewModel(
	private val checkDatabaseUpdateRequired: CheckDatabaseUpdateRequired,
	private val setDatabaseState: SetDatabaseState,
): ViewModel() {

	private val _activityFragment: MutableStateFlow<ActivityFragment> = MutableStateFlow(ActivityFragment.HOME)
	val activityFragment: StateFlow<ActivityFragment> get() = _activityFragment

	private val _updateDbState: MutableStateFlow<LocalDate?> = MutableStateFlow(null)
	val updateDbState: StateFlow<LocalDate?> get() = _updateDbState

	private val _showAlert: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val showAlert: StateFlow<Boolean> get() = _showAlert



	fun setActivityType(activityFragment: ActivityFragment){
		//prevents spamming and rerendering of the same fragment...
		if (activityFragment != _activityFragment.value)
			_activityFragment.value = activityFragment
	}

	fun setUpdateDbState(day: Long){
		viewModelScope.launch {
			setDatabaseState(LocalDate.now().plusDays(day))
			//FIXME setup the app theme?
		}
	}

}