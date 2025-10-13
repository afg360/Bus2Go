package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository

class AddTag(
	private val stmFavouritesRepository: StmFavouritesRepository,
	private val exoFavouritesRepository: ExoFavouritesRepository
){
	suspend operator fun invoke(tag: Tag, favourites: List<TransitData>) {
		val stmItems = favourites.filter { it is StmBusItem }
		val exoItems = favourites.filter { it is ExoTrainItem || it is ExoBusItem }
		stmFavouritesRepository.setTag(tag, stmItems)
		exoFavouritesRepository.setTag(tag, exoItems)
	}
}