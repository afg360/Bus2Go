package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import kotlinx.coroutines.flow.Flow

//TODO output a result object (in case of failure, we will retry...), or maybe a boolean...
interface StmFavouritesRepository: FavouritesRepository {

	fun getStmBusFavourites(): Flow<List<StmBusFavouriteItem>>

	suspend fun removeStmBusFavourite(data : StmBusFavouriteItem)

	suspend fun addStmBusFavourite(data : StmBusFavouriteItem)
}