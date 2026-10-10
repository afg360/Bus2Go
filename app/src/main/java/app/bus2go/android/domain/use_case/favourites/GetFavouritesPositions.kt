package app.bus2go.android.domain.use_case.favourites

import app.bus2go.android.data.data_source.local.datastore.favourites_position.entity.PositionDto
import app.bus2go.android.domain.repository.FavouritesPositionRepository
import kotlinx.coroutines.flow.Flow

class GetFavouritesPositions(
	private val favouritesPosition: FavouritesPositionRepository,
) {

	operator fun invoke(): Flow<List<PositionDto>> {
		return favouritesPosition.getFavouritesPositions()
	}
}