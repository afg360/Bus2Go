package app.bus2go.android.domain.use_case.favourites

import app.bus2go.android.domain.repository.FavouritesPositionRepository

class MoveFavourite(
	private val favouritesPositionRepository: FavouritesPositionRepository
) {

	suspend operator fun invoke(oldPosition: Int, newPosition: Int) {
		favouritesPositionRepository.moveFavourite(oldPosition, newPosition)
	}

}