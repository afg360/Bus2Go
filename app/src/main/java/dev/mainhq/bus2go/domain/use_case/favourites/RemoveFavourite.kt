package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository

class RemoveFavourite(
	private val stmFavouritesRepository: StmFavouritesRepository,
	private val exoFavouritesRepository: ExoFavouritesRepository,
	private val favouritesPositionRepository: FavouritesPositionRepository
) {

	suspend operator fun invoke(favourite: FavouriteTransitData){
		when(favourite){
			is StmBusFavouriteItem -> stmFavouritesRepository.removeStmBusFavourite(favourite)
			is ExoTrainFavouriteItem -> exoFavouritesRepository.removeExoTrainFavourite(favourite)
			is ExoBusFavouriteItem -> exoFavouritesRepository.removeExoBusFavourite(favourite)
		}
		favouritesPositionRepository.removeFavourite(favourite)
	}
}
