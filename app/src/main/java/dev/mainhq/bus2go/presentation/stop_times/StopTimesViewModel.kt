package dev.mainhq.bus2go.presentation.stop_times

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.presentation.main.home.favourites.Urgency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

class StopTimesViewModel(
	private val getTransitTime: GetTransitTime,
	private val transitData: TransitData
): ViewModel() {

	private val _stopTimesHeaderDisplayModel: MutableStateFlow<StopTimesHeaderDisplayModel?> = MutableStateFlow(null)
	val stopTimesHeaderDisplayModel = _stopTimesHeaderDisplayModel.asStateFlow()

	private val _arrivalTimes: MutableStateFlow<List<StopTimesDisplayModel>?> = MutableStateFlow(null)
	val arrivalTimes = _arrivalTimes.asStateFlow()

	//TODO caches the last time for use in the last 5 min just in case
	private val _lastTime: MutableStateFlow<Time?> = MutableStateFlow(null)
	val lastTime = _lastTime.asStateFlow()

	init {
		_stopTimesHeaderDisplayModel.value = when(transitData){
			is ExoBusItem -> StopTimesHeaderDisplayModel(
				R.color.basic_purple,
				if (transitData.routeId.length < 10) 35f else 24f,
				transitData.routeId,
				transitData.headsign,
				transitData.stopName
			)
			is ExoTrainItem -> StopTimesHeaderDisplayModel(
				R.color.orange,
				if (transitData.routeName.length < 10) 35f else 24f,
				//FIXME use a string resource here...
				"Train ${transitData.routeName}",
				transitData.direction,
				transitData.stopName
			)
			is StmBusItem -> {
				StopTimesHeaderDisplayModel(
					R.color.basic_blue,
					if (transitData.routeId.length < 10) 35f else 24f,
					transitData.routeId,
					//remove anything inside parenthesis to reduce text...
					transitData.lastStop.replace(Regex("\\(.*\\)"), ""),
					transitData.stopName
				)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			while(true){

				when(val transitTime = getTransitTime.invoke(Time.now(), transitData)){
					is Result.Error -> {
						TODO()
					}

					is Result.Success<List<Time>> -> {
						_arrivalTimes.value = transitTime.data.map{
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
					}
				}

				delay(1000)
			}
		}
	}
}