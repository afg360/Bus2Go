package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository
import dev.mainhq.bus2go.domain.repository.FavouritesRepository

class RemoveFavourite(
	private val favouritesRepos: List<FavouritesRepository>,
	private val favouritesPositionRepository: FavouritesPositionRepository
) {

	suspend operator fun invoke(favourite: FavouriteTransitData){
		when(favourite){
			is StmBusFavouriteItem -> {
				favouritesRepos.find { it.transitType == TransitType.STM }!!
					.removeFavourite(favourite)
			}
			is ExoBusFavouriteItem -> {
				favouritesRepos.find { it.transitType == TransitType.EXO_BUS }!!
					.removeFavourite(favourite)
			}
			is ExoTrainFavouriteItem -> {
				favouritesRepos.find { it.transitType == TransitType.EXO_TRAIN }!!
					.removeFavourite(favourite)
			}
		}
		favouritesPositionRepository.removeFavourite(favourite)
	}
}
