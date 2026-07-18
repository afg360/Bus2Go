package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import dev.mainhq.bus2go.utils.queryRepos

class AddTag(
	private val favouritesRepository: List<FavouritesRepository>,
){
	suspend operator fun invoke(tag: Tag, favourites: List<FavouriteTransitData>) {
		favourites.filter { it is StmBusFavouriteItem }.also {
			favouritesRepository.queryRepos(TransitType.STM).setTag(tag, it)
		}
		favourites.filter { it is ExoBusFavouriteItem }.also {
			favouritesRepository.queryRepos(TransitType.EXO_BUS).setTag(tag, it)
		}
		favourites.filter { it is ExoTrainFavouriteItem }.also {
			favouritesRepository.queryRepos(TransitType.EXO_TRAIN).setTag(tag, it)
		}
	}
}