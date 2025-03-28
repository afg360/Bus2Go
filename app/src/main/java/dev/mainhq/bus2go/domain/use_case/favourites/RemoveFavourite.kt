package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository

//FIXMe perhaps isntead make a removeFavourites (multiple ones)
class RemoveFavourite(
	private val exoFavouritesRepo: ExoFavouritesRepository,
	private val stmFavouritesRepository: StmFavouritesRepository
) {

	suspend operator fun invoke(favourite: TransitData){
		//val stmFavourites = favourites.filterIsInstance<StmFavouriteBusItem>()
		//val exoBusFavourites = favourites.filterIsInstance<ExoFavouriteBusItem>()
		//val exoTrainFavourites = favourites.filterIsInstance<ExoFavouriteTrainItem>()
		when(favourite){
			is StmBusItem -> stmFavouritesRepository.removeStmBusFavourite(favourite)
			is ExoTrainItem -> exoFavouritesRepo.removeExoTrainFavourite(favourite)
			is ExoBusItem -> exoFavouritesRepo.removeExoBusFavourite(favourite)
		}
	}
}
