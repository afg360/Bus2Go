package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StmFavouritesRepositoryImpl(
	private val tagsHandler: TagsHandler, //TODO make as a Datastore instead?
	private val stmFavouritesDataStore: DataStore<StmFavouritesDataDto>
) : FavouritesRepository {

	override val transitType: TransitType
		get() = TransitType.STM

	override val favourites: Flow<List<FavouriteTransitData>>
		get() {
			return stmFavouritesDataStore.data.map { PreferenceMapper.mapStmBus(it) }
		}

	override suspend fun removeFavourite(data: FavouriteTransitData) {
		data as StmBusFavouriteItem
		withContext(Dispatchers.IO){
			stmFavouritesDataStore.updateData { stmFavouritesData ->
				stmFavouritesData.copy(listSTM = stmFavouritesData.listSTM.mutate {
					it.remove(PreferenceMapper.mapStmBusToDto(data))
				})
			}
		}
	}

	override suspend fun addFavourite(data: FavouriteTransitData) {
		data as StmBusFavouriteItem
		withContext(Dispatchers.IO){
			stmFavouritesDataStore.updateData { stmFavouritesData ->
				stmFavouritesData.copy(listSTM = stmFavouritesData.listSTM.mutate {
					//FIXME will that add data uniquely...?
					it.add(PreferenceMapper.mapStmBusToDto(data))
				})
			}
		}
	}

	override suspend fun setTag(tag: Tag, items: List<FavouriteTransitData>) {
		val tagDto = PreferenceMapper.mapTagToDto(tag)
		tagsHandler.addTag(tagDto)
		stmFavouritesDataStore.updateData { favourites ->
			favourites.copy(
				listSTM = favourites.listSTM.mutate { mutableList ->
					val inputItems = items.filter { it is StmBusFavouriteItem }
						.map { PreferenceMapper.mapStmBusToDto(it as StmBusFavouriteItem) }

					mutableList.filter { inputItems.contains(it) && !it.tags.contains(tagDto) }
						.forEach { it.tags.mutate { mutableTags -> mutableTags.add(tagDto) } }
				}
			)
		}
	}

	override suspend fun getTags(): List<Tag> {
		return tagsHandler.readTags().map { PreferenceMapper.mapTag(it) }
	}
}