package app.bus2go.android.domain.use_case.transit

import app.bus2go.android.domain.core.Result
import app.bus2go.android.domain.entity.ExoBusItem
import app.bus2go.android.domain.entity.ExoTrainItem
import app.bus2go.android.domain.entity.TransitData
import app.bus2go.android.domain.entity.StmBusItem
import app.bus2go.android.domain.entity.Time
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.repository.TransitRepository
import app.bus2go.android.presentation.core.UiState
import app.bus2go.android.presentation.main.home.Urgency
import app.bus2go.android.presentation.stop_times.StopTimesDisplayModel
import app.bus2go.android.utils.queryRepos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Used inside the Times Activity to retrieve
 * all the current scheduled transit for the rest of the day.
 * **/
class GetTransitTime(
	private val transitRepos: List<TransitRepository>
) {

	/**
	 * @param curTime The time from which to display the transit times. Usually corresponds to [Time.now].
	 **/
	operator fun invoke(transitData: TransitData, curTime: Time?): Flow<UiState<List<StopTimesDisplayModel>>> {
		return flow {
			emit(UiState.Loading)
			val transitType = when(transitData){
				is StmBusItem -> TransitType.STM
				is ExoBusItem -> TransitType.EXO_BUS
				is ExoTrainItem -> TransitType.EXO_TRAIN
			}

			var running = true
			while(running) {
				val timeToUse = curTime ?: Time.now()
				when(val transitTime = transitRepos.queryRepos(transitType).getStopTimes(transitData, timeToUse)) {
					is Result.Error -> {
						running = false
						//FIXME show some sort of error
						emit(
							UiState.Error("Error trying to get the time: ${transitTime.message}", null)
						)
					}
					is Result.Success<List<Time>> -> {
						emit(
							UiState.Success(
								transitTime.data.map {
									val timeRemaining = it.timeRemaining()
									val timeLeftTextDisplay = if (curTime == null) timeRemaining?.let {
										//FIXMe instead of checking hour, check if smaller than an hour
										if (timeRemaining.toHours().toInt() == 0) timeRemaining.toMinutes().toString()
										else "In >> 1h" //string that will be replaced by the resource value
									} ?: "Passed bus???"
									else ""

									val urgency = if (timeRemaining == null || timeRemaining < Duration.ofMinutes(4))
										Urgency.IMMINENT
									else if (timeRemaining < Duration.ofMinutes(15)) Urgency.SOON
									else Urgency.DISTANT
									StopTimesDisplayModel(
										arrivalTime = it,
										timeLeftTextDisplay = timeLeftTextDisplay,
										urgency = urgency
									)
								}
							)
						)
					}
				}
				delay(1000.milliseconds)
			}
		}
	}
}