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
import dev.mainhq.bus2go.domain.use_case.db_state.GetDatabaseExpiryDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class StopTimesViewModel(
	private val transitData: TransitData,
	private val getTransitTime: GetTransitTime,
	private val getDatabaseExpiryDate: GetDatabaseExpiryDate
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


	private val _chosenDate: MutableStateFlow<Time?> = MutableStateFlow(null)
	val chosenDate = _chosenDate.asStateFlow()

	@OptIn(ExperimentalCoroutinesApi::class)
	val arrivalTimes = _chosenDate.flatMapLatest {
		getTransitTime.invoke(transitData, it)
			.stateIn(
				viewModelScope,
				SharingStarted.WhileSubscribed(5000),
				emptyList()
			)
	}

	fun setChosenDate(millis: Long) {
		_chosenDate.update {
			//FIXME somehow millis is selectedDate - 1, so add a day
			val chosenDate = Time.fromMillis(millis + 24 * 3600 * 1000).resetTime()
			if (chosenDate == Time.now().resetTime()) null
			else chosenDate
		}
	}

	//represents the day the data will expire
	val maxCalendarDate = getDatabaseExpiryDate.invoke(transitData)
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			null
		)


	//TODO caches the last time for use in the last 5 min just in case
	private val _lastTime: MutableStateFlow<Time?> = MutableStateFlow(null)
	val lastTime = _lastTime.asStateFlow()

}
