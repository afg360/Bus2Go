package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ExoFavouritesRepositoryImpl(
	private val tagsHandler: TagsHandler,
	private val exoFavouritesDataStore: DataStore<ExoFavouritesDataDto>
): ExoFavouritesRepository {
	override suspend fun getExoBusFavourites(): List<ExoBusItem> {
		return withContext(Dispatchers.IO) {
			PreferenceMapper.mapExoBus(exoFavouritesDataStore.data.first())
		}
	}

	override suspend fun getExoTrainFavourites(): List<ExoTrainItem> {
		return withContext(Dispatchers.IO) {
			PreferenceMapper.mapExoTrain(exoFavouritesDataStore.data.first())
		}
	}

	override suspend fun removeExoBusFavourite(data: ExoBusItem) {
		withContext(Dispatchers.IO) {
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExo = favourites.listExo.mutate {
					//maybe add a tripid or some identifier so that it is a unique thing deleted
					it.remove(PreferenceMapper.mapExoBusToDto(data))
				})
			}
		}
	}

	override suspend fun removeExoTrainFavourite(data: ExoTrainItem) {
		withContext(Dispatchers.IO) {
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
					//maybe add a tripid or some identifier so that it is a unique thing deleted
					it.remove(PreferenceMapper.mapExoTrainToDto(data))
				})
			}
		}
	}

	override suspend fun addExoBusFavourite(data: ExoBusItem) {
		withContext(Dispatchers.IO) {
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExo = favourites.listExo.mutate {
					it.add(PreferenceMapper.mapExoBusToDto(data))
				})
			}
		}
	}

	override suspend fun addExoTrainFavourite(data: ExoTrainItem) {
		withContext(Dispatchers.IO) {
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
					it.add(PreferenceMapper.mapExoTrainToDto(data))
				})
			}
		}
	}

	override suspend fun setTag(
		tag: Tag,
		items: List<TransitData>,
	) {
		val tagDto = PreferenceMapper.mapTagToDto(tag)
		tagsHandler.addTag(tagDto)
		exoFavouritesDataStore.updateData { favourites ->
			favourites.copy(
				listExo = favourites.listExo.mutate { mutableList ->
					val inputItems = items.filter { it is ExoBusItem }
						.map { PreferenceMapper.mapExoBusToDto(it as ExoBusItem) }

					mutableList.filter { inputItems.contains(it) && !it.tags.contains(tagDto) }
						.forEach { it.tags.mutate { mutableTags -> mutableTags.add(tagDto) } }
				},
				listExoTrain = favourites.listExoTrain.mutate { mutableList ->
					val inputItems = items.filter { it is ExoTrainItem }
						.map { PreferenceMapper.mapExoTrainToDto(it as ExoTrainItem) }

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