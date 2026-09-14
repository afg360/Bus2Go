package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class FakeStmFavouritesRepo(): FavouritesRepository {

	override val transitType = TransitType.STM

	val tags = mutableListOf(Tag("STM", 0x1234))

	override val favourites = MutableStateFlow(
		(1..10).map {
			StmBusFavouriteItem(
				UUID.randomUUID(),
				it.toString(),
				"Stm Bus $it",
				"foo",
				tags,
				1,
				"lastStop"
			)
		}
	)

	override suspend fun removeFavourite(data: FavouriteTransitData) {
		favourites.update { list ->
			list.toMutableList().apply {
				remove(data as StmBusFavouriteItem)
			}
		}
	}

	override suspend fun addFavourite(data: FavouriteTransitData) {
		favourites.update { list ->
			list.toMutableList().apply {
				//assert it is an StmBusFavouriteItem
				add(data as StmBusFavouriteItem)
			}
		}
	}

	override suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>) {
		if (!tags.contains(tag)) {
			tags += tag
		}
		favourites.update { list ->
			list.map {
				if (items.contains(it)) {
					StmBusFavouriteItem(
						it.id,
						it.routeId,
						it.stopName,
						it.direction,
						it.tags + tag,
						it.directionId,
						it.lastStop
					)
				}
				else it
			}
		}
	}

	override suspend fun getTags() = tags

}