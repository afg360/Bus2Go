package app.bus2go.android.domain.use_case.favourites

import app.bus2go.android.domain.entity.TransitData
import app.bus2go.android.domain.entity.FavouriteTransitData
import app.bus2go.android.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import app.bus2go.android.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import app.bus2go.android.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.repository.FavouritesPositionRepository
import app.bus2go.android.domain.repository.FavouritesRepository
import app.bus2go.android.utils.queryRepos

class AddFavourite(
	private val favouritesRepos: List<FavouritesRepository>,
	private val favouritesPositionRepository: FavouritesPositionRepository
) {

	suspend operator fun invoke(transitData: TransitData){
		//FIXME need algo for getting correct position to put in
		val favourite = FavouriteTransitData.fromTransitDataToFavouriteTransitData(transitData)
		when(favourite){
			is StmBusFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.STM).addFavourite(favourite)
			}
			is ExoBusFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.EXO_BUS).addFavourite(favourite)
			}
			is ExoTrainFavouriteItem -> {
				favouritesRepos.queryRepos(TransitType.EXO_TRAIN).addFavourite(favourite)
			}
		}
		favouritesPositionRepository.addFavourite(favourite)
	}
}