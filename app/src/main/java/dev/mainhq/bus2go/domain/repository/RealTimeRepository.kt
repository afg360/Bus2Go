package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.StmBusItem

interface RealTimeRepository {

	suspend fun getRealTime(data: List<StmBusItem>): Int
}