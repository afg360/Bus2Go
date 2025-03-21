package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.entity.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.ExoFavouriteTrainItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.StmFavouriteBusItem
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

	suspend operator fun invoke(transitData: FavouriteTransitData): List<Time>{
		val curTime = Time.now()
		when(transitData){
			is ExoFavouriteBusItem -> return exoRepository.getStopTimes(
				transitData,
				curTime
			)
			is ExoFavouriteTrainItem -> return exoRepository.getStopTimes(
				transitData,
				curTime
			)

			is StmFavouriteBusItem -> return stmRepository.getStopTimes(
				transitData,
				curTime
			)

		}

	}
}