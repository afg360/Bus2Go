package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.TransitData

interface FavouritesRepository {
	suspend fun setTag(tag: String, items: List<TransitData>)

	suspend fun getFavouritesFromTag(tag: String): Result<List<TransitData>>
}