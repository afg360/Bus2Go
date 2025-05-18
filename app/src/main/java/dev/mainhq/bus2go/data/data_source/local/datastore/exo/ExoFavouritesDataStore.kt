package dev.mainhq.bus2go.data.data_source.local.datastore.exo

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.MigrationObserver
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.favouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteTrainItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataSerializer
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import java.io.File

val Context.exoFavouritesDataStore by dataStore(
	fileName = "favourites_exo.json",
	serializer = ExoFavouritesDataSerializer,
	produceMigrations = { context -> listOf(
		object : DataMigration<ExoFavouritesDataDto> {
			override suspend fun cleanUp() {
				MigrationObserver.notifyExoCleanUpReady(context)
			}

			override suspend fun shouldMigrate(currentData: ExoFavouritesDataDto): Boolean {
				return File(context.filesDir.resolve("datastore"), "favourites.json").exists()
						|| File(context.filesDir.resolve("datastore"), "favourites_1.json").exists()
			}

			override suspend fun migrate(currentData: ExoFavouritesDataDto): ExoFavouritesDataDto {
				return currentData.copy(
					version = 1,
					listExo = context.favouritesDataStore.data.first()
						.listExo.map {
							ExoFavouriteBusItemDto(it.stopName, it.routeId, it.direction, it.routeLongName)
						}.toPersistentList(),
					listExoTrain = context.favouritesDataStore.data.first()
						.listExoTrain.map {
							ExoFavouriteTrainItemDto(it.stopName, it.routeId, it.trainNum, it.routeName, it.directionId, it.direction)
						}.toPersistentList()
				)
			}

		}
	)}
)
