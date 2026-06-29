package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class FakeExoFavouritesRepo: ExoFavouritesRepository {
	val exoBusItems = (1..10).map {
		ExoBusFavouriteItem(
			UUID.randomUUID(),
			it.toString(),
			"Exo Bus $it",
			"foo",
			tags = listOf(Tag("Exo", 0x000)),
			"lastStop"
		)
	}.toMutableList()

	val exoTrainItems = (1..5).map {
		ExoTrainFavouriteItem(
			UUID.randomUUID(),
			it.toString(),
			"Stm Bus $it",
			"foo",
			tags = listOf(Tag("Train", 0xFFF)),
			it,
			"lastStop",
			1
		)
	}.toMutableList()

	val tags = mutableListOf(
		Tag("Train", 0xFFF),
		Tag("Exo", 0x000),
	)

	override fun getExoBusFavourites(): Flow<List<ExoBusFavouriteItem>> {
		return flow { emit(exoBusItems) }
	}

	override fun getExoTrainFavourites(): Flow<List<ExoTrainFavouriteItem>> {
		return flow { emit(exoTrainItems) }
	}

	override suspend fun removeExoBusFavourite(data: ExoBusFavouriteItem) {
		exoBusItems.remove(data)
	}

	override suspend fun removeExoTrainFavourite(data: ExoTrainFavouriteItem) {
		exoTrainItems.remove(data)
	}

	override suspend fun addExoBusFavourite(data: ExoBusFavouriteItem) {
		exoBusItems.add(data)
	}

	override suspend fun addExoTrainFavourite(data: ExoTrainFavouriteItem) {
		exoTrainItems.add(data)
	}

	override suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>) {
		TODO("Not Implemented")
	}

	override suspend fun getTags(): List<Tag> {
		return tags
	}
}