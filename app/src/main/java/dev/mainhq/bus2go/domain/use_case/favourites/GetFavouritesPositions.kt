package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity.PositionDto
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository
import kotlinx.coroutines.flow.Flow

class GetFavouritesPositions(
	private val favouritesPosition: FavouritesPositionRepository,
) {

	operator fun invoke(): Flow<List<PositionDto>> {
		return favouritesPosition.getFavouritesPositions()
	}
}