package dev.mainhq.bus2go.data.repository

import android.content.Context
import dev.mainhq.bus2go.domain.entity.StmFavouriteBusItem
import dev.mainhq.bus2go.domain.repository.RealTimeRepository

class RealTimeRepositoryImpl(
	private val context: Context
): RealTimeRepository {
	override suspend fun getRealTime(data: List<StmFavouriteBusItem>): Int {
		TODO("Not yet implemented")
	}
}