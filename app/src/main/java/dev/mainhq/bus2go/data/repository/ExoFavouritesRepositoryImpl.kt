package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExoFavouritesRepositoryImpl(
	private val tagsHandler: TagsHandler,
	private val exoFavouritesDataStore: DataStore<ExoFavouritesDataDto>,
): FavouritesRepository {

	override val transitType: TransitType
		get() = TransitType.EXO_BUS

	override val favourites: Flow<List<FavouriteTransitData>>
		get() {
			return exoFavouritesDataStore.data.map { PreferenceMapper.mapExoBus(it) }
		}

	override suspend fun removeFavourite(data: FavouriteTransitData) {
		data as ExoBusFavouriteItem
		withContext(Dispatchers.IO) {
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExo = favourites.listExo.mutate {
					//maybe add a tripid or some identifier so that it is a unique thing deleted
					it.remove(PreferenceMapper.mapExoBusToDto(data))
				})
			}
		}
	}


	override suspend fun addFavourite(data: FavouriteTransitData) {
		data as ExoBusFavouriteItem
		withContext(Dispatchers.IO) {
			exoFavouritesDataStore.updateData { favourites ->
				favourites.copy(listExo = favourites.listExo.mutate {
					it.add(PreferenceMapper.mapExoBusToDto(data))
				})
			}
		}
	}

	override suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>) {
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
			)
		}
	}

	//perhaps should not be defined in this interface...
	override suspend fun getTags(): List<Tag> {
		return tagsHandler.readTags().map { PreferenceMapper.mapTag(it) }
	}
}