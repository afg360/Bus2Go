package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import dev.mainhq.bus2go.utils.queryRepos

class RemoveFavourite(
	private val favouritesRepos: List<FavouritesRepository>,
	private val favouritesPositionRepository: FavouritesPositionRepository
) {

	suspend operator fun invoke(favourite: FavouriteTransitData){
		when(favourite){
			is StmBusFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.STM).removeFavourite(favourite)
			}
			is ExoBusFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.EXO_BUS).removeFavourite(favourite)
			}
			is ExoTrainFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.EXO_TRAIN).removeFavourite(favourite)
			}
		}
		favouritesPositionRepository.removeFavourite(favourite)
	}
}
