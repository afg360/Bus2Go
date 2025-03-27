package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteTrainItem
import dev.mainhq.bus2go.domain.entity.stm.StmFavouriteBusItem
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
			is StmFavouriteBusItem -> stmFavouritesRepository.removeStmBusFavourite(favourite)
			is ExoFavouriteTrainItem -> exoFavouritesRepo.removeExoTrainFavourite(favourite)
			is ExoFavouriteBusItem -> exoFavouritesRepo.removeExoBusFavourite(favourite)
		}
	}
}
