package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository

class GetFavourites(
	private val exoFavouritesRepository: ExoFavouritesRepository,
	private val stmFavouritesRepository: StmFavouritesRepository
) {

	suspend operator fun invoke(): List<TransitData>{
		return stmFavouritesRepository.getStmBusFavourites() + exoFavouritesRepository.getExoBusFavourites() +
				exoFavouritesRepository.getExoTrainFavourites()
	}
}