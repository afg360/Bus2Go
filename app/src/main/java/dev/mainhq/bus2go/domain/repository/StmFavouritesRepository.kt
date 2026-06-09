package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.StmBusItem
import kotlinx.coroutines.flow.Flow

//TODO output a result object (in case of failure, we will retry...), or maybe a boolean...
interface StmFavouritesRepository: FavouritesRepository {

	fun getStmBusFavourites(): Flow<List<StmBusItem>>

	suspend fun removeStmBusFavourite(data : StmBusItem)

	suspend fun addStmBusFavourite(data : StmBusItem)
}