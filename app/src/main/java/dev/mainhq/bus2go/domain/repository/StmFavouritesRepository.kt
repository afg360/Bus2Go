package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.StmBusItem

interface StmFavouritesRepository {

	suspend fun getStmBusFavourites(): List<StmBusItem>

	suspend fun removeStmBusFavourite(data : StmBusItem)

	suspend fun addStmBusFavourite(data : StmBusItem)
}