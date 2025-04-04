package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository

class FakeStmFavouritesRepo: StmFavouritesRepository {

	val stmItems = (1..10).map {
		StmBusItem(
			it.toString(),
			"Stm Bus $it",
			"foo",
			1,
			"lastStop"
		)
	}.toMutableList()

	override suspend fun getStmBusFavourites(): List<StmBusItem> {
		return stmItems
	}

	override suspend fun removeStmBusFavourite(data: StmBusItem) {
		stmItems.remove(data)
	}

	override suspend fun addStmBusFavourite(data: StmBusItem) {
		stmItems.add(data)
	}

}