package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import kotlinx.coroutines.flow.Flow

//TODO output a result object (in case of failure, we will retry...), or maybe a boolean...
interface ExoFavouritesRepository: FavouritesRepository {

	fun getExoBusFavourites(): Flow<List<ExoBusFavouriteItem>>
	fun getExoTrainFavourites(): Flow<List<ExoTrainFavouriteItem>>

	suspend fun removeExoBusFavourite(data : ExoBusFavouriteItem)
	suspend fun removeExoTrainFavourite(data : ExoTrainFavouriteItem)

	suspend fun addExoBusFavourite(data : ExoBusFavouriteItem)
	suspend fun addExoTrainFavourite(data : ExoTrainFavouriteItem)

}