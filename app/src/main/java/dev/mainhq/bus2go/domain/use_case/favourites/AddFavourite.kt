package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.ExoFavouriteTrainItem
import dev.mainhq.bus2go.domain.entity.StmFavouriteBusItem
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository

class AddFavourite(
	private val exoFavouritesRepo: ExoFavouritesRepository,
	private val stmFavouritesRepository: StmFavouritesRepository
) {

	suspend operator fun invoke(favourite: FavouriteTransitData){
		when(favourite){
			is StmFavouriteBusItem -> stmFavouritesRepository.addStmBusFavourite(favourite)
			is ExoFavouriteTrainItem -> exoFavouritesRepo.addExoTrainFavourite(favourite)
			is ExoFavouriteBusItem -> exoFavouritesRepo.addExoBusFavourite(favourite)
		}
	}
}