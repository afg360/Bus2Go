package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository

class AddTag(
	private val favouritesRepository: List<FavouritesRepository>,
){
	suspend operator fun invoke(tag: Tag, favourites: List<FavouriteTransitData>) {
		val stmItems = favourites.filter { it is StmBusFavouriteItem }
		val exoItems = favourites.filter { it is ExoTrainFavouriteItem || it is ExoBusFavouriteItem }
		favouritesRepository.find { it.transitType == TransitType.STM }!!.setTag(tag, stmItems)
		favouritesRepository.filter { it.transitType == TransitType.EXO_BUS || it.transitType == TransitType.EXO_TRAIN }!!
			.forEach { it.setTag(tag, exoItems) }
	}
}