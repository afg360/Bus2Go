package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.Tag

interface FavouritesRepository {
	suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>)
	suspend fun getTags(): List<Tag>

	//TODO perhaps instead of using transitData, use directly the old position and find the data with that old position?
	// but this could lead to bugs if in the middle of processing...
	suspend fun setFavouritePosition(favouriteTransitData: FavouriteTransitData, newPosition: Int)
}