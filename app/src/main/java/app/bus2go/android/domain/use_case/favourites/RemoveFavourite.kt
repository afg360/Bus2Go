package app.bus2go.android.domain.use_case.favourites

import app.bus2go.android.domain.entity.FavouriteTransitData
import app.bus2go.android.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import app.bus2go.android.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import app.bus2go.android.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.repository.FavouritesPositionRepository
import app.bus2go.android.domain.repository.FavouritesRepository
import app.bus2go.android.utils.queryRepos

class RemoveFavourite(
	private val favouritesRepos: List<FavouritesRepository>,
	private val favouritesPositionRepository: FavouritesPositionRepository
) {

	suspend operator fun invoke(favourite: FavouriteTransitData){
		when(favourite){
			is StmBusFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.STM).removeFavourite(favourite)
			}
			is ExoBusFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.EXO_BUS).removeFavourite(favourite)
			}
			is ExoTrainFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.EXO_TRAIN).removeFavourite(favourite)
			}
		}
		favouritesPositionRepository.removeFavourite(favourite)
	}
}
