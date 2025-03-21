package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.preference.PreferenceMapper
import dev.mainhq.bus2go.data.data_source.local.preference.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteTrainItem
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.first

class ExoFavouritesRepositoryImpl(private val exoFavouritesDataStore: DataStore<ExoFavouritesDataDto>): ExoFavouritesRepository {
	override suspend fun getExoBusFavourites(): List<ExoFavouriteBusItem> {
		return PreferenceMapper.mapExoBus(exoFavouritesDataStore.data.first())
	}

	override suspend fun getExoTrainFavourites(): List<ExoFavouriteTrainItem> {
		return PreferenceMapper.mapExoTrain(exoFavouritesDataStore.data.first())
	}

	override suspend fun removeExoBusFavourite(data: ExoFavouriteBusItem) {
		exoFavouritesDataStore.updateData {favourites ->
			favourites.copy(listExo = favourites.listExo.mutate {
				//maybe add a tripid or some identifier so that it is a unique thing deleted
				it.remove(PreferenceMapper.mapExoBusToDto(data))
			})
		}
	}

	override suspend fun removeExoTrainFavourite(data: ExoFavouriteTrainItem) {
		exoFavouritesDataStore.updateData {favourites ->
			favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
				//maybe add a tripid or some identifier so that it is a unique thing deleted
				it.remove(PreferenceMapper.mapExoTrainToDto(data))
			})
		}
	}

	override suspend fun addExoBusFavourite(data: ExoFavouriteBusItem) {
		exoFavouritesDataStore.updateData { favourites ->
			favourites.copy(listExo = favourites.listExo.mutate {
				it.add(PreferenceMapper.mapExoBusToDto(data))
			})
		}
	}

	override suspend fun addExoTrainFavourite(data: ExoFavouriteTrainItem) {
		exoFavouritesDataStore.updateData { favourites ->
			favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
				it.add(PreferenceMapper.mapExoTrainToDto(data))
			})
		}
	}

}