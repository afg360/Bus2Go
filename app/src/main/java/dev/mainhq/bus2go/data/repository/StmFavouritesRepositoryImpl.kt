package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.StmFavouritesDataDto1
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.Tags
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class StmFavouritesRepositoryImpl(
	private val tags: Tags,
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

	override suspend fun setTag(
		tag: String,
		items: List<TransitData>,
	) {
		tags.addTag(tag)
		TODO("Not yet implemented")
	}

	override suspend fun getFavouritesFromTag(tag: String): Result<List<TransitData>> {
		TODO("Not yet implemented")
	}

}