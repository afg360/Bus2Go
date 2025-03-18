package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteTrainItem

interface ExoFavouritesRepository {

	suspend fun getExoBusFavourites(): List<ExoFavouriteBusItem>
	suspend fun getExoTrainFavourites(): List<ExoFavouriteTrainItem>

	suspend fun removeExoBusFavourite(data : ExoFavouriteBusItem)
	suspend fun removeExoTrainFavourite(data : ExoFavouriteTrainItem)

	suspend fun addExoBusFavourite(data : ExoFavouriteBusItem)
	suspend fun addExoTrainFavourite(data : ExoFavouriteTrainItem)

}