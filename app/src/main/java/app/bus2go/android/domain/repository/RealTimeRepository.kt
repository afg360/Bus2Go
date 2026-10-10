package app.bus2go.android.domain.repository

import app.bus2go.android.domain.entity.StmBusItem

interface RealTimeRepository {

	suspend fun getRealTime(data: List<StmBusItem>): Int
}