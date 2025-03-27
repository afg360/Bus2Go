package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository

class AddFavourite(
	private val exoFavouritesRepo: ExoFavouritesRepository,
	private val stmFavouritesRepository: StmFavouritesRepository
) {

	suspend operator fun invoke(favourite: TransitData){
		when(favourite){
			is StmBusItem -> stmFavouritesRepository.addStmBusFavourite(favourite)
			is ExoTrainItem -> exoFavouritesRepo.addExoTrainFavourite(favourite)
			is ExoBusItem -> exoFavouritesRepo.addExoBusFavourite(favourite)
		}
	}
}