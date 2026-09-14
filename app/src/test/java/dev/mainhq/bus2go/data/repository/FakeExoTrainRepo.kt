package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class FakeExoTrainRepo(): FavouritesRepository {

	override val transitType = TransitType.EXO_TRAIN

	private val tags = mutableListOf(Tag("Train", 0x000))

	override val favourites = MutableStateFlow(
			(1..5).map {
			ExoTrainFavouriteItem(
				UUID.randomUUID(),
				it.toString(),
				"Stm Bus $it",
				"foo",
				tags,
				it,
				"lastStop",
				1
			)
		}
	)


	override suspend fun removeFavourite(data: FavouriteTransitData) {
		favourites.update { list ->
			list.toMutableList().apply {
				remove(data as ExoTrainFavouriteItem)
			}
		}
	}

	override suspend fun addFavourite(data: FavouriteTransitData) {
		favourites.update { list ->
			list.toMutableList().apply {
				add(data as ExoTrainFavouriteItem)
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
					ExoTrainFavouriteItem(
						it.id,
						it.routeId,
						it.stopName,
						it.direction,
						it.tags + tag,
						it.trainNum,
						it.routeName,
						it.directionId
					)
				}
				else it
			}
		}
	}

	override suspend fun getTags() = tags
}