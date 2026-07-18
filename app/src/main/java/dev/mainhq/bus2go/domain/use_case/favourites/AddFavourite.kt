package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import dev.mainhq.bus2go.utils.queryRepos

class AddFavourite(
	private val favouritesRepos: List<FavouritesRepository>,
	private val favouritesPositionRepository: FavouritesPositionRepository
) {

	suspend operator fun invoke(transitData: TransitData){
		//FIXME need algo for getting correct position to put in
		val favourite = FavouriteTransitData.fromTransitDataToFavouriteTransitData(transitData)
		when(favourite){
			is StmBusFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.STM).addFavourite(favourite)
			}
			is ExoBusFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.EXO_BUS).addFavourite(favourite)
			}
			is ExoTrainFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.EXO_TRAIN).addFavourite(favourite)
			}
		}
		favouritesPositionRepository.addFavourite(favourite)
	}
}