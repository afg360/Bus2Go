package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoBusData
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoTrainData

interface ExoFavouritesRepository {

	fun getExoBusFavourites(): List<ExoBusData>
	fun getExoTrainFavourites(): List<ExoTrainData>

	fun removeExoBusFavourite(data : ExoBusData)
	fun removeExoTrainFavourite(data : ExoTrainData)

	fun addExoBusFavourite(data : ExoBusData)
	fun addExoTrainFavourite(data : ExoTrainData)

}