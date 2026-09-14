package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import java.util.UUID

class FakeExoFavouritesRepo(): FavouritesRepository {
	override val transitType = TransitType.EXO_BUS

	private val tags = mutableListOf(Tag("Exo", 0x000))

	override val favourites = MutableStateFlow(
		(1..10).map {
			ExoBusFavouriteItem(
				UUID.randomUUID(),
				it.toString(),
				"Exo Bus $it",
				"foo",
				tags,
				"lastStop"
			)
		}
	)

	override suspend fun removeFavourite(data: FavouriteTransitData) {
		favourites.update { list ->
			list.toMutableList().apply {
				remove(data as ExoBusFavouriteItem)
			}
		}
	}

	override suspend fun addFavourite(data: FavouriteTransitData) {
		favourites.update { list ->
			list.toMutableList().apply {
				//assert it is an ExoBusFavouriteItem
				add(data as ExoBusFavouriteItem)
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
					ExoBusFavouriteItem(
						it.id,
						it.routeId,
						it.stopName,
						it.direction,
						it.tags + tag,
						it.routeLongName
					)
				}
				else it
			}
		}
	}

	override suspend fun getTags() = tags
}