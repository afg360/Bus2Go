package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem

//TODO output a result object (in case of failure, we will retry...), or maybe a boolean...
interface ExoFavouritesRepository: FavouritesRepository {

	suspend fun getExoBusFavourites(): List<ExoBusItem>
	suspend fun getExoTrainFavourites(): List<ExoTrainItem>

	suspend fun removeExoBusFavourite(data : ExoBusItem)
	suspend fun removeExoTrainFavourite(data : ExoTrainItem)

	suspend fun addExoBusFavourite(data : ExoBusItem)
	suspend fun addExoTrainFavourite(data : ExoTrainItem)

}