package dev.mainhq.bus2go.presentation.stop_direction

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StopDirectionViewModel: ViewModel() {

	private val _activityFragment = MutableStateFlow(ActivityFragment.DIRECTION)
	val activityFragment = _activityFragment.asStateFlow()


	fun setActivityFragment(activityFragment: ActivityFragment){
		_activityFragment.value = activityFragment
	}
}