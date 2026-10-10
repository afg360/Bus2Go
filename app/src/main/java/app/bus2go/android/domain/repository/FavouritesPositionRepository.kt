package app.bus2go.android.domain.repository

import app.bus2go.android.data.data_source.local.datastore.favourites_position.entity.PositionDto
import app.bus2go.android.domain.entity.FavouriteTransitData
import kotlinx.coroutines.flow.Flow

interface FavouritesPositionRepository {
	//FIXME change to a non DTO class
	fun getFavouritesPositions(): Flow<List<PositionDto>>
	suspend fun addFavourite(favourite: FavouriteTransitData)
	suspend fun removeFavourite(favourite: FavouriteTransitData)
	suspend fun moveFavourite(oldPosition: Int, newPosition: Int)
}