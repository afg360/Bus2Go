package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.TransitRepository
import dev.mainhq.bus2go.presentation.main.home.Urgency
import dev.mainhq.bus2go.presentation.stop_times.StopTimesDisplayModel
import dev.mainhq.bus2go.utils.queryRepos
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

	operator fun invoke(transitData: TransitData): Flow<List<StopTimesDisplayModel>> {
		val transitType = when(transitData){
			is StmBusItem -> TransitType.STM
			is ExoBusItem -> TransitType.EXO_BUS
			is ExoTrainItem -> TransitType.EXO_TRAIN
		}

		return flow {
			var running = true
			while(running) {
				when(val transitTime = transitRepos.queryRepos(transitType).getStopTimes(transitData, Time.now())) {
					is Result.Error -> {
						//TODO show some sort of error
						running = false
						emit(
							listOf(
								StopTimesDisplayModel(
									arrivalTime = Time.now(),
									timeLeftTextDisplay = "Error trying to get the time: ${transitTime.message}",
									urgency = Urgency.DISTANT
								)
							)
						)
					}
					is Result.Success<List<Time>> -> {
						emit(
							transitTime.data.map{
								val timeRemaining = it.timeRemaining()
								val timeLeftTextDisplay = timeRemaining?.let {
									//FIXMe instead of checking hour, check if smaller than an hour
									if (timeRemaining.toHours().toInt() == 0) timeRemaining.toMinutes().toString()
									else "" //empty string that will be replaced by the resource value
								} ?: "Passed bus???"
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
					}
				}
				delay(1000.milliseconds)
			}
		}
	}
}