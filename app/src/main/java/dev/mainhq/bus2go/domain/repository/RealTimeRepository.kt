package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.StmFavouriteBusItem

interface RealTimeRepository {

	suspend fun getRealTime(data: List<StmFavouriteBusItem>): Int
}