package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.TransitRepository
import dev.mainhq.bus2go.utils.queryRepos

/**
 * Used inside the Times Activity to retrieve
 * all the current scheduled transit for the rest of the day.
 * **/
class GetTransitTime(
	private val transitRepos: List<TransitRepository>
) {

	suspend operator fun invoke(curTime: Time, transitData: TransitData): Result<List<Time>> {
		return when(transitData){
			is StmBusItem -> {
				transitRepos.queryRepos(TransitType.STM).getStopTimes(transitData, curTime)
			}

			is ExoBusItem -> {
				transitRepos.queryRepos(TransitType.EXO_BUS).getStopTimes(transitData, curTime)
			}

			is ExoTrainItem -> {
				transitRepos.queryRepos(TransitType.EXO_TRAIN).getStopTimes(transitData, curTime)
			}

		}

	}
}