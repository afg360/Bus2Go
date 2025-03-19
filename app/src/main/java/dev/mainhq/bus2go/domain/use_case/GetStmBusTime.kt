package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.utils.Time

class GetStmBusTime(
	private val stmRepository: StmRepository
) {

	//TODO perhaps use a flow of list instead of a simple list
	suspend operator fun invoke(stmTransitData: FavouriteTransitData): List<Time>{
		return stmRepository.getStopTimes(
			stmTransitData,
			Time.now()
		)
	}
}