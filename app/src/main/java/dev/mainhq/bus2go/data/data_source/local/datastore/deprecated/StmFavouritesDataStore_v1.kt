package dev.mainhq.bus2go.data.data_source.local.datastore.deprecated

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.MigrationObserver
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import java.io.File

@Deprecated("Use newer version, which introduces tags for each stm item")
val Context.stmFavouritesDataStore_v1 by dataStore(
	fileName = "favourites_stm.json",
	serializer = StmFavouritesDataSerializer1,
	produceMigrations = { context -> listOf(
		object : DataMigration<StmFavouritesDataDto1> {
			override suspend fun cleanUp() {
				MigrationObserver.notifyStmCleanUpReady(context)
			}

			override suspend fun shouldMigrate(currentData: StmFavouritesDataDto1): Boolean {
				return File(context.filesDir.resolve("datastore"), "favourites.json").exists()
						|| File(context.filesDir.resolve("datastore"), "favourites_1.json").exists()
			}

			override suspend fun migrate(currentData: StmFavouritesDataDto1): StmFavouritesDataDto1 {
				return currentData.copy(
					version = 1,
					//here since context.favouritesDataStore is called, migration of the other one will
					//already be performed, so no need to worry about how to notify it to migrate
					listSTM = context.favouritesDataStore.data.first()
						.listSTM.map {
							StmFavouriteBusItemDto_v1(it.stopName, it.routeId, it.directionId, it.direction, it.lastStop)
						}.toPersistentList()
				)
			}
		}
	)}
)
