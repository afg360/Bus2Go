package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity.FavouritesPositionDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity.PositionDto
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository
import dev.mainhq.bus2go.utils.swap
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavouritePositionRepositoryImpl(
	private val favouritesPositionDataStore: DataStore<FavouritesPositionDataDto>
): FavouritesPositionRepository {

	override fun getFavouritesPositions(): Flow<List<PositionDto>> {
		return favouritesPositionDataStore.data.map { it.listFavouritesPosition.toList() }
	}

	override suspend fun addFavourite(favourite: FavouriteTransitData) {
		favouritesPositionDataStore.updateData { favourites ->
			favourites.copy(listFavouritesPosition = favourites.listFavouritesPosition.mutate { list ->
				val transitType = when(favourite) {
					is FavouriteTransitData.StmBusFavouriteItem -> TransitType.STM
					is FavouriteTransitData.ExoBusFavouriteItem -> TransitType.EXO_BUS
					is FavouriteTransitData.ExoTrainFavouriteItem -> TransitType.EXO_TRAIN
				}
				list.add(PositionDto(favourite.id.toString(), transitType))
			})
		}
	}

	override suspend fun removeFavourite(favourite: FavouriteTransitData) {
		favouritesPositionDataStore.updateData { favourites ->
			favourites.copy(listFavouritesPosition = favourites.listFavouritesPosition.mutate { list ->
				list.remove(list.first { it.itemId == favourite.id.toString() })
			})
		}
	}

	override suspend fun moveFavourite(oldPosition: Int, newPosition: Int) {
		favouritesPositionDataStore.updateData { favourites ->
			favourites.copy(listFavouritesPosition = favourites.listFavouritesPosition.mutate { list ->
				list.swap(oldPosition, newPosition)
			})
		}
	}
}