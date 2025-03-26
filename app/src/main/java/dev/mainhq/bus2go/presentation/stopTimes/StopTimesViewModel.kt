package dev.mainhq.bus2go.presentation.stopTimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime
import dev.mainhq.bus2go.utils.Time
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StopTimesViewModel(
	private val getTransitTime: GetTransitTime,
	private val transitData: FavouriteTransitData
): ViewModel() {

	private val _arrivalTimes: MutableStateFlow<List<Time>> = MutableStateFlow(listOf())
	val arrivalTimes = _arrivalTimes.asStateFlow()

	//TODO caches the last time for use in the last 5 min just in case
	private val _lastTime: MutableStateFlow<Time?> = MutableStateFlow(null)
	val lastTime = _lastTime.asStateFlow()

	init {
		viewModelScope.launch {
			while(true){
				_arrivalTimes.value = getTransitTime(transitData)
				delay(5000)
			}
		}
	}
}