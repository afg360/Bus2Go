package dev.mainhq.bus2go.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dev.mainhq.bus2go.utils.ActivityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivityViewModel: ViewModel() {

	private val _activityType: MutableStateFlow<ActivityType> = MutableStateFlow(ActivityType.HOME)
	val activityType: StateFlow<ActivityType> get() = _activityType

	fun setActivityType(activityType: ActivityType){
		//prevents spamming and rerendering of the same fragment...
		if (activityType != _activityType.value)
			_activityType.value = activityType
	}

}