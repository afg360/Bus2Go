package dev.mainhq.bus2go.presentation.stop_times

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime
import dev.mainhq.bus2go.domain.entity.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class StopTimesViewModel(
	private val getTransitTime: GetTransitTime,
	private val transitData: TransitData
): ViewModel() {

	val stopTimesHeaderDisplayModel = when(transitData){
		is ExoBusItem -> StopTimesHeaderDisplayModel(
			R.color.basic_purple,
			if (transitData.routeId.length < 10) 35f else 24f,
			transitData.routeId,
			transitData.direction,
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


	val arrivalTimes = getTransitTime.invoke(transitData)
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			emptyList()
		)

	//TODO caches the last time for use in the last 5 min just in case
	private val _lastTime: MutableStateFlow<Time?> = MutableStateFlow(null)
	val lastTime = _lastTime.asStateFlow()

}
