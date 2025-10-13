package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitData
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class StmFavouritesRepositoryImpl(
	private val tagsHandler: TagsHandler,
	private val stmFavouritesDataStore: DataStore<StmFavouritesDataDto>
) : StmFavouritesRepository {

	override suspend fun getStmBusFavourites(): List<StmBusItem> {
		return withContext(Dispatchers.IO) {
			PreferenceMapper.mapStmBus(stmFavouritesDataStore.data.first())
		}
	}

	override suspend fun removeStmBusFavourite(data: StmBusItem) {
		withContext(Dispatchers.IO){
			stmFavouritesDataStore.updateData { stmFavouritesData ->
				stmFavouritesData.copy(listSTM = stmFavouritesData.listSTM.mutate {
					it.remove(PreferenceMapper.mapStmBusToDto(data))
				})
			}
		}
	}

	override suspend fun addStmBusFavourite(data: StmBusItem) {
		withContext(Dispatchers.IO){
			stmFavouritesDataStore.updateData { stmFavouritesData ->
				stmFavouritesData.copy(listSTM = stmFavouritesData.listSTM.mutate {
					//FIXME will that add data uniquely...?
					it.add(PreferenceMapper.mapStmBusToDto(data))
				})
			}
		}
	}

	override suspend fun setTag(tag: Tag, items: List<TransitData>) {
		val tagDto = PreferenceMapper.mapTagToDto(tag)
		tagsHandler.addTag(tagDto)
		stmFavouritesDataStore.updateData { favourites ->
			favourites.copy(
				listSTM = favourites.listSTM.mutate { mutableList ->
					val inputItems = items.filter { it is StmBusItem }
						.map { PreferenceMapper.mapStmBusToDto(it as StmBusItem) }

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