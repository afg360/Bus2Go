package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository

class AddFavourite(
	private val exoFavouritesRepository: ExoFavouritesRepository,
	private val stmFavouritesRepository: StmFavouritesRepository
) {

	suspend operator fun invoke(transitData: TransitData){
		//FIXME need algo for getting correct position to put in
		val favourite = FavouriteTransitData.fromTransitDataToFavouriteTransitData(transitData, )
		when(favourite){
			is StmBusFavouriteItem -> {
				stmFavouritesRepository.addStmBusFavourite(favourite)
			}
			is ExoBusFavouriteItem -> {
				exoFavouritesRepository.addExoBusFavourite(favourite)
			}
			is ExoTrainFavouriteItem -> {
				exoFavouritesRepository.addExoTrainFavourite(favourite)
			}
		}
	}
}