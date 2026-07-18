package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitType
import kotlinx.coroutines.flow.Flow

interface FavouritesRepository {
	val transitType: TransitType
	val favourites: Flow<List<FavouriteTransitData>>
	suspend fun removeFavourite(data : FavouriteTransitData)
	suspend fun addFavourite(data : FavouriteTransitData)

	suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>)
	suspend fun getTags(): List<Tag>

}