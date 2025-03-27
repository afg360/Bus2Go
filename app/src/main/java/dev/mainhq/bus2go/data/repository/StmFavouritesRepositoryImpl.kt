package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.domain.entity.StmBusItem
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class StmFavouritesRepositoryImpl(private val stmFavouritesDataStore: DataStore<StmFavouritesDataDto>)
	: StmFavouritesRepository {

		//TODO withContext(Dispatchers.IO) forall suspend fxn doing heavy computation

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

}