package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity.PositionDto
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import kotlinx.coroutines.flow.Flow

interface FavouritesPositionRepository {
	//FIXME change to a non DTO class
	fun getFavouritesPositions(): Flow<List<PositionDto>>
	suspend fun addFavourite(favourite: FavouriteTransitData)
	suspend fun removeFavourite(favourite: FavouriteTransitData)
	suspend fun moveFavourite(oldPosition: Int, newPosition: Int)
}