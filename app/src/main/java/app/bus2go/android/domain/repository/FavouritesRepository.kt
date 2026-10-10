package app.bus2go.android.domain.repository

import app.bus2go.android.domain.entity.FavouriteTransitData
import app.bus2go.android.domain.entity.Tag
import app.bus2go.android.domain.entity.TransitType
import kotlinx.coroutines.flow.Flow

interface FavouritesRepository {
	val transitType: TransitType
	val favourites: Flow<List<FavouriteTransitData>>
	suspend fun removeFavourite(data : FavouriteTransitData)
	suspend fun addFavourite(data : FavouriteTransitData)

	suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>)
	suspend fun getTags(): List<Tag>

}