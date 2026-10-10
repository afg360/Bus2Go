package app.bus2go.android.domain.use_case.favourites

import app.bus2go.android.domain.entity.Tag
import app.bus2go.android.domain.entity.FavouriteTransitData
import app.bus2go.android.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import app.bus2go.android.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import app.bus2go.android.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.repository.FavouritesRepository
import app.bus2go.android.utils.queryRepos

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