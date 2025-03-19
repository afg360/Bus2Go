package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.utils.Time

//FIXME wtf is the purpose of this class, same as GetExoBusTime
class GetExoTrainTime (
		private val exoRepository: ExoRepository
) {

	suspend operator fun invoke(exoTransitData: FavouriteTransitData): List<Time>{
		return exoRepository.getStopTimes(
			exoTransitData,
			Time.now()
		)
	}
}