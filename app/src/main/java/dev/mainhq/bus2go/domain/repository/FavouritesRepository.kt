package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.Tag

interface FavouritesRepository {
	suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>)
	suspend fun getTags(): List<Tag>
}