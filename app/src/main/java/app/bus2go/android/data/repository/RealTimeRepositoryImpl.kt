package app.bus2go.android.data.repository

import android.content.Context
import app.bus2go.android.domain.entity.StmBusItem
import app.bus2go.android.domain.repository.RealTimeRepository

class RealTimeRepositoryImpl(
	private val context: Context
): RealTimeRepository {
	override suspend fun getRealTime(data: List<StmBusItem>): Int {
		TODO("Not yet implemented")
	}
}