package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.ExoFavouriteTrainItem

interface ExoFavouritesRepository {

	suspend fun getExoBusFavourites(): List<ExoFavouriteBusItem>
	suspend fun getExoTrainFavourites(): List<ExoFavouriteTrainItem>

	suspend fun removeExoBusFavourite(data : ExoFavouriteBusItem)
	suspend fun removeExoTrainFavourite(data : ExoFavouriteTrainItem)

	suspend fun addExoBusFavourite(data : ExoFavouriteBusItem)
	suspend fun addExoTrainFavourite(data : ExoFavouriteTrainItem)

}