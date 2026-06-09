package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import kotlinx.coroutines.flow.Flow

//TODO output a result object (in case of failure, we will retry...), or maybe a boolean...
interface ExoFavouritesRepository: FavouritesRepository {

	fun getExoBusFavourites(): Flow<List<ExoBusItem>>
	fun getExoTrainFavourites(): Flow<List<ExoTrainItem>>

	suspend fun removeExoBusFavourite(data : ExoBusItem)
	suspend fun removeExoTrainFavourite(data : ExoTrainItem)

	suspend fun addExoBusFavourite(data : ExoBusItem)
	suspend fun addExoTrainFavourite(data : ExoTrainItem)

}