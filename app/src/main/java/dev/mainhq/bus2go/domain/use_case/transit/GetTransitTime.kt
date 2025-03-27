package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.utils.Time

/**
 * Used inside the Times Activity to retrieve
 * all the current scheduled transit for the rest of the day.
 * **/
class GetTransitTime(
	private val exoRepository: ExoRepository,
	private val stmRepository: StmRepository,
) {

	suspend operator fun invoke(transitData: TransitData): List<Time>{
		val curTime = Time.now()
		when(transitData){
			is ExoBusItem -> return exoRepository.getStopTimes(
				transitData,
				curTime
			)
			is ExoTrainItem -> return exoRepository.getStopTimes(
				transitData,
				curTime
			)

			is StmBusItem -> return stmRepository.getStopTimes(
				transitData,
				curTime
			)

		}

	}
}