package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository

class MoveFavourite(
	private val favouritesPositionRepository: FavouritesPositionRepository
) {

	suspend operator fun invoke(oldPosition: Int, newPosition: Int) {
		favouritesPositionRepository.moveFavourite(oldPosition, newPosition)
	}

}