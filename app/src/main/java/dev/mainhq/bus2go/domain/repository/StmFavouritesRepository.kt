package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.StmFavouriteBusItem

interface StmFavouritesRepository {

	suspend fun getStmBusFavourites(): List<StmFavouriteBusItem>

	suspend fun removeStmBusFavourite(data : StmFavouriteBusItem)

	suspend fun addStmBusFavourite(data : StmFavouriteBusItem)
}