package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class FakeStmFavouritesRepo: StmFavouritesRepository {

	val stmItems = (1..10).map {
		StmBusFavouriteItem(
			UUID.randomUUID(),
			it.toString(),
			"Stm Bus $it",
			"foo",
			listOf(Tag("STM", 0x1234)),
			1,
			"lastStop"
		)
	}.toMutableList()

	val tags = mutableListOf(Tag("STM", 0x1234))

	override fun getStmBusFavourites(): Flow<List<StmBusFavouriteItem>> {
		return flow { emit(stmItems) }
	}

	override suspend fun removeStmBusFavourite(data: StmBusFavouriteItem) {
		stmItems.remove(data)
	}

	override suspend fun addStmBusFavourite(data: StmBusFavouriteItem) {
		stmItems.add(data)
	}

	override suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>) {
		TODO("Not yet implemented")
	}

	override suspend fun getTags(): List<Tag> {
		return tags
	}

}