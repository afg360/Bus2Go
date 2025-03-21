package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.preference.PreferenceMapper
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import dev.mainhq.bus2go.data.data_source.local.preference.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.domain.entity.stm.StmFavouriteBusItem
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.first

class StmFavouritesRepositoryImpl(private val stmFavouritesDataStore: DataStore<StmFavouritesDataDto>)
	: StmFavouritesRepository {

	override suspend fun getStmBusFavourites(): List<StmFavouriteBusItem> {
		return PreferenceMapper.mapStmBus(stmFavouritesDataStore.data.first())
	}

	override suspend fun removeStmBusFavourite(data: StmFavouriteBusItem) {
		stmFavouritesDataStore.updateData { stmFavouritesData ->
            stmFavouritesData.copy(listSTM = stmFavouritesData.listSTM.mutate {
				it.remove(PreferenceMapper.mapStmBusToDto(data))
            })
        }
	}

	override suspend fun addStmBusFavourite(data: StmFavouriteBusItem) {
		stmFavouritesDataStore.updateData { stmFavouritesData ->
			stmFavouritesData.copy(listSTM = stmFavouritesData.listSTM.mutate {
				//FIXME will that add data uniquely...?
				it.add(PreferenceMapper.mapStmBusToDto(data))
			})
		}
	}

}