package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.StmBusItem

//TODO output a result object (in case of failure, we will retry...), or maybe a boolean...
interface StmFavouritesRepository: FavouritesRepository {

	suspend fun getStmBusFavourites(): List<StmBusItem>

	suspend fun removeStmBusFavourite(data : StmBusItem)

	suspend fun addStmBusFavourite(data : StmBusItem)
}