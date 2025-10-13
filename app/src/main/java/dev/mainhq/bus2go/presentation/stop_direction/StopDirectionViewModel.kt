package dev.mainhq.bus2go.presentation.stop_direction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StopDirectionViewModel: ViewModel() {

	private val _activityFragment: MutableStateFlow<ActivityFragment> =
			MutableStateFlow(ActivityFragment.Direction)
	val activityFragment = _activityFragment.asStateFlow()

	private val _fromTimesActivity: MutableSharedFlow<Boolean> = MutableSharedFlow()
	val fromTimesActivity = _fromTimesActivity.asSharedFlow()

	fun setActivityFragment(activityFragment: ActivityFragment){
		_activityFragment.update { activityFragment }
	}

	//TODO
	fun toTimesActivity(){
		viewModelScope.launch {
			_fromTimesActivity.emit(true)
		}
	}
}