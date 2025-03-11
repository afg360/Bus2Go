package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.StmBusData

interface StmFavouritesRepository {

	fun getStmBusFavourites(): List<StmBusData>

	fun removeStmBusFavourite(data : StmBusData)

	fun addStmBusFavourite(data : StmBusData)
}