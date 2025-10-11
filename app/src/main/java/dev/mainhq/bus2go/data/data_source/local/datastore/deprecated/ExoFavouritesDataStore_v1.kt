package dev.mainhq.bus2go.data.data_source.local.datastore.deprecated

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataSerializer
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import java.io.File

@Deprecated("Use v2")
val Context.exoFavouritesDataStore_v1 by dataStore(
	fileName = "favourites_exo.json",
	serializer = ExoFavouritesDataSerializer_v1,
	produceMigrations = { context -> listOf(
		object : DataMigration<ExoFavouritesDataDto_v1> {
			override suspend fun cleanUp() {
				MigrationObserver.notifyExoCleanUpReady(context)
			}

			override suspend fun shouldMigrate(currentData: ExoFavouritesDataDto_v1): Boolean {
				return File(context.filesDir.resolve("datastore"), "favourites.json").exists()
						|| File(context.filesDir.resolve("datastore"), "favourites_1.json").exists()
			}

			override suspend fun migrate(currentData: ExoFavouritesDataDto_v1): ExoFavouritesDataDto_v1 {
				return currentData.copy(
					version = 1,
					listExo = context.favouritesDataStore.data.first()
						.listExo.map {
							ExoFavouriteBusItemDto_v1(it.stopName, it.routeId, it.direction, it.routeLongName)
						}.toPersistentList(),
					listExoTrain = context.favouritesDataStore.data.first()
						.listExoTrain.map {
							ExoFavouriteTrainItemDto_v1(it.stopName, it.routeId, it.trainNum, it.routeName, it.directionId, it.direction)
						}.toPersistentList()
				)
			}

		}
	)}
)
