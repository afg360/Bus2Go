package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository

class FakeExoFavouritesRepo: ExoFavouritesRepository {
	val exoBusItems = (1..10).map {
		ExoBusItem(
			it.toString(),
			"Stm Bus $it",
			"foo",
			it.toString(),
			"lastStop"
		)
	}.toMutableList()

	val exoTrainItems = (1..10).map {
		ExoTrainItem(
			it.toString(),
			"Stm Bus $it",
			"foo",
			it,
			"lastStop",
			1
		)
	}.toMutableList()

	override suspend fun getExoBusFavourites(): List<ExoBusItem> {
		return exoBusItems
	}

	override suspend fun getExoTrainFavourites(): List<ExoTrainItem> {
		return exoTrainItems
	}

	override suspend fun removeExoBusFavourite(data: ExoBusItem) {
		exoBusItems.remove(data)
	}

	override suspend fun removeExoTrainFavourite(data: ExoTrainItem) {
		exoTrainItems.remove(data)
	}

	override suspend fun addExoBusFavourite(data: ExoBusItem) {
		exoBusItems.add(data)
	}

	override suspend fun addExoTrainFavourite(data: ExoTrainItem) {
		exoTrainItems.add(data)
	}
}