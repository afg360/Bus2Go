package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository

class SortFavourites(
	private val exoFavouritesRepository: ExoFavouritesRepository,
	private val stmFavouritesRepository: StmFavouritesRepository,
) {

	suspend operator fun invoke() {
//		exoFavouritesRepository.
	}

}