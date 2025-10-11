package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.Tags
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ExoFavouritesRepositoryImpl(
	private val tags: Tags,
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
		tag: String,
		items: List<TransitData>,
	) {
		TODO("Not yet implemented")
	}

	override suspend fun getFavouritesFromTag(tag: String): Result<List<TransitData>> {
		TODO("Not yet implemented")
	}

}