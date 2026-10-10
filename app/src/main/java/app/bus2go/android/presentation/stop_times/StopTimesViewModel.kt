package app.bus2go.android.presentation.stop_times

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.R as MaterialRes
import app.bus2go.android.R
import app.bus2go.android.domain.entity.ExoBusItem
import app.bus2go.android.domain.entity.ExoTrainItem
import app.bus2go.android.domain.entity.StmBusItem
import app.bus2go.android.domain.entity.TransitData
import app.bus2go.android.domain.use_case.transit.GetTransitTime
import app.bus2go.android.domain.entity.Time
import app.bus2go.android.domain.use_case.db_state.GetDatabaseExpiryDate
import app.bus2go.android.presentation.core.UiState
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
			R.attr.colorCustomColor1,
			if (transitData.routeId.length < 10) 35f else 24f,
			transitData.routeId,
			transitData.direction,
			transitData.stopName
		)
		is ExoTrainItem -> StopTimesHeaderDisplayModel(
			R.attr.colorCustomColor2,
			if (transitData.routeName.length < 10) 35f else 24f,
			//FIXME use a string resource here...
			"Train ${transitData.routeName}",
			transitData.direction,
			transitData.stopName
		)
		is StmBusItem -> {
			StopTimesHeaderDisplayModel(
				MaterialRes.attr.colorPrimary,
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
				UiState.Loading
			)
	}

	fun setChosenDate(millis: Long) {
		_chosenDate.update {
			//Need to add a day, since time selected is at midnight, which would go to the day before
			val chosenDate = Time.fromMillis(millis + 24 * 60 * 60 * 1000).resetTime()
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
