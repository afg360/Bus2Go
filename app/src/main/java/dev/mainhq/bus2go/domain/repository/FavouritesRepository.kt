package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitData

interface FavouritesRepository {
	suspend fun setTag(tag: Tag, items: List<TransitData>)
	suspend fun getTags(): List<Tag>
}