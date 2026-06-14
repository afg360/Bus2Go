package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity.FavouritesPositionDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity.PositionDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExoFavouritesRepositoryImpl(
	private val tagsHandler: TagsHandler,
	private val favouritesPositionDataStore: DataStore<FavouritesPositionDataDto>,
	private val exoFavouritesDataStore: DataStore<ExoFavouritesDataDto>
): ExoFavouritesRepository {
	override fun getExoBusFavourites(): Flow<List<ExoBusFavouriteItem>> {
		return exoFavouritesDataStore.data.map { PreferenceMapper.mapExoBus(it) }
	}

	override fun getExoTrainFavourites(): Flow<List<ExoTrainFavouriteItem>> {
		return exoFavouritesDataStore.data.map { PreferenceMapper.mapExoTrain(it) }
	}

	override suspend fun removeExoBusFavourite(data: ExoBusFavouriteItem) {
		withContext(Dispatchers.IO) {
			favouritesPositionDataStore.updateData { favourites ->
				favourites.copy(listFavouritesPosition = favourites.listFavouritesPosition.mutate { list ->
					list.remove(list.first { it.itemId == data.id.toString() })
				})
			}
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExo = favourites.listExo.mutate {
					//maybe add a tripid or some identifier so that it is a unique thing deleted
					it.remove(PreferenceMapper.mapExoBusToDto(data))
				})
			}
		}
	}

	override suspend fun removeExoTrainFavourite(data: ExoTrainFavouriteItem) {
		withContext(Dispatchers.IO) {
			favouritesPositionDataStore.updateData { favourites ->
				favourites.copy(listFavouritesPosition = favourites.listFavouritesPosition.mutate { list ->
					list.remove(list.first { it.itemId == data.id.toString() })
				})
			}
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
					//maybe add a tripid or some identifier so that it is a unique thing deleted
					it.remove(PreferenceMapper.mapExoTrainToDto(data))
				})
			}
		}
	}

	override suspend fun addExoBusFavourite(data: ExoBusFavouriteItem) {
		withContext(Dispatchers.IO) {
			favouritesPositionDataStore.updateData { favourites ->
				favourites.copy(listFavouritesPosition = favourites.listFavouritesPosition.mutate { list ->
					list.add(PositionDto(data.id.toString(), TransitType.EXO_BUS))
				})
			}
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExo = favourites.listExo.mutate {
					it.add(PreferenceMapper.mapExoBusToDto(data))
				})
			}
		}
	}

	override suspend fun addExoTrainFavourite(data: ExoTrainFavouriteItem) {
		withContext(Dispatchers.IO) {
			favouritesPositionDataStore.updateData { favourites ->
				favourites.copy(listFavouritesPosition = favourites.listFavouritesPosition.mutate { list ->
					list.add(PositionDto(data.id.toString(), TransitType.EXO_TRAIN))
				})
			}
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
					it.add(PreferenceMapper.mapExoTrainToDto(data))
				})
			}
		}
	}

	override suspend fun setFavouritePosition(
		favouriteTransitData: FavouriteTransitData,
		newPosition: Int,
	) {
		TODO("Not yet implemented")
	}

	override suspend fun setTag(
		tag: Tag,
		items: List<FavouriteTransitData>,
	) {
		val tagDto = PreferenceMapper.mapTagToDto(tag)
		tagsHandler.addTag(tagDto)
		exoFavouritesDataStore.updateData { favourites ->
			favourites.copy(
				listExo = favourites.listExo.mutate { mutableList ->
					val inputItems = items.filter { it is ExoBusFavouriteItem }
						.map { PreferenceMapper.mapExoBusToDto(it as ExoBusFavouriteItem) }

					mutableList.filter { inputItems.contains(it) && !it.tags.contains(tagDto) }
						.forEach { it.tags.mutate { mutableTags -> mutableTags.add(tagDto) } }
				},
				listExoTrain = favourites.listExoTrain.mutate { mutableList ->
					val inputItems = items.filter { it is ExoTrainFavouriteItem }
						.map { PreferenceMapper.mapExoTrainToDto(it as ExoTrainFavouriteItem) }

					mutableList.filter { inputItems.contains(it) && !it.tags.contains(tagDto) }
						.forEach { it.tags.mutate { mutableTags -> mutableTags.add(tagDto) } }
				}
			)
		}
	}

	//perhaps should not be defined in this interface...
	override suspend fun getTags(): List<Tag> {
		return tagsHandler.readTags().map { PreferenceMapper.mapTag(it) }
	}
}