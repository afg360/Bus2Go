package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExoTrainFavouritesRepositoryImpl(
	private val exoFavouritesDataStore: DataStore<ExoFavouritesDataDto>,
	private val tagsHandler: TagsHandler
): FavouritesRepository {

	override val transitType: TransitType
		get() = TransitType.EXO_TRAIN

	override val favourites: Flow<List<FavouriteTransitData>>
		get() {
			return exoFavouritesDataStore.data.map { PreferenceMapper.mapExoTrain(it) }
		}

	override suspend fun removeFavourite(data: FavouriteTransitData) {
		data as FavouriteTransitData.ExoTrainFavouriteItem
		withContext(Dispatchers.IO) {
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
					//maybe add a tripid or some identifier so that it is a unique thing deleted
					it.remove(PreferenceMapper.mapExoTrainToDto(data))
				})
			}
		}
	}

	override suspend fun addFavourite(data: FavouriteTransitData) {
		data as FavouriteTransitData.ExoTrainFavouriteItem
		withContext(Dispatchers.IO) {
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
					it.add(PreferenceMapper.mapExoTrainToDto(data))
				})
			}
		}
	}

	override suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>) {
		val tagDto = PreferenceMapper.mapTagToDto(tag)
		tagsHandler.addTag(tagDto)
		exoFavouritesDataStore.updateData { favourites ->
			favourites.copy(
				listExoTrain = favourites.listExoTrain.mutate { mutableList ->
					val inputItems = items.filter { it is FavouriteTransitData.ExoTrainFavouriteItem }
						.map { PreferenceMapper.mapExoTrainToDto(it as FavouriteTransitData.ExoTrainFavouriteItem) }

					mutableList.filter { inputItems.contains(it) && !it.tags.contains(tagDto) }
						.forEach { it.tags.mutate { mutableTags -> mutableTags.add(tagDto) } }
				}
			)
		}
	}

	override suspend fun getTags(): List<Tag> {
		//perhaps should not be defined in this interface...
		return tagsHandler.readTags().map { PreferenceMapper.mapTag(it) }
	}

}