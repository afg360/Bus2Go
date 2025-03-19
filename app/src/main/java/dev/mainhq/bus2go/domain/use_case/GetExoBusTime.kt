package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.utils.Time

class GetExoBusTime(
	private val exoRepository: ExoRepository
) {

	suspend operator fun invoke(exoTransitData: FavouriteTransitData): List<Time>{
		return exoRepository.getStopTimes(
			exoTransitData,
			Time.now()
		)
	}
}