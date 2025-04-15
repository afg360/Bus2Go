package dev.mainhq.bus2go.presentation.stopTimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.presentation.main.home.favourites.Urgency
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

class StopTimesViewModel(
	private val getTransitTime: GetTransitTime,
	private val transitData: TransitData
): ViewModel() {

	private val _displayText: MutableStateFlow<String?> = MutableStateFlow(null)
	val displayText = _displayText.asStateFlow()

	private val _arrivalTimes: MutableStateFlow<List<StopTimesDisplayModel>?> = MutableStateFlow(null)
	val arrivalTimes = _arrivalTimes.asStateFlow()

	//TODO caches the last time for use in the last 5 min just in case
	private val _lastTime: MutableStateFlow<Time?> = MutableStateFlow(null)
	val lastTime = _lastTime.asStateFlow()

	init {
		_displayText.value = when(transitData){
			is ExoBusItem -> "${transitData.routeId} ${transitData.stopName} -> ${transitData.headsign}"
			is ExoTrainItem -> "#${transitData.trainNum} ${transitData.stopName} -> ${transitData.direction}"
			is StmBusItem -> "${transitData.routeId} ${transitData.stopName} -> ${transitData.direction}"
		}
		viewModelScope.launch {
			while(true){
				_arrivalTimes.value = getTransitTime(Time.now(), transitData).map {
					val timeRemaining = it.timeRemaining()
					val timeLeftTextDisplay = timeRemaining?.let {
						//FIXMe instead of checking hour, check if smaller than an hour
						if (timeRemaining.hour == 0) timeRemaining.minute.toString()
						else "" //empty string that will be replaced by the resource value
					} ?: "Passed bus???"
					val urgency = if (timeRemaining == null || timeRemaining < LocalTime.of(0, 4, 0))
						Urgency.IMMINENT
					else if (timeRemaining < LocalTime.of(0, 15, 0)) Urgency.SOON
					else Urgency.DISTANT
					StopTimesDisplayModel(
						arrivalTime = it,
						timeLeftTextDisplay = timeLeftTextDisplay,
						urgency = urgency
					)
				}
				delay(1000)
			}
		}
	}
}