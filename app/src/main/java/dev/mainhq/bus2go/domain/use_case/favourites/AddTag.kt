package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem

class AddTag(
	private val stmFavouritesRepository: StmFavouritesRepository,
	private val exoFavouritesRepository: ExoFavouritesRepository
){
	suspend operator fun invoke(tag: Tag, favourites: List<FavouriteTransitData>) {
		val stmItems = favourites.filter { it is StmBusFavouriteItem }
		val exoItems = favourites.filter { it is ExoTrainFavouriteItem || it is ExoBusFavouriteItem }
		stmFavouritesRepository.setTag(tag, stmItems)
		exoFavouritesRepository.setTag(tag, exoItems)
	}
}