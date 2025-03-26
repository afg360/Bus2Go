package dev.mainhq.bus2go.data.data_source.local.datastore.stm

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.MigrationObserver
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.favouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataSerializer
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import java.io.File


val Context.stmFavouritesDataStore by dataStore(
	fileName = "favourites_stm.json",
	serializer = StmFavouritesDataSerializer,
	produceMigrations = { context -> listOf(
		object : DataMigration<StmFavouritesDataDto> {
			override suspend fun cleanUp() {
				MigrationObserver.notifyStmCleanUpReady(context)
			}

			override suspend fun shouldMigrate(currentData: StmFavouritesDataDto): Boolean {
				return File(context.filesDir.resolve("datastore"), "favourites.json").exists()
						|| File(context.filesDir.resolve("datastore"), "favourites_1.json").exists()
			}

			override suspend fun migrate(currentData: StmFavouritesDataDto): StmFavouritesDataDto {
				return currentData.copy(
					version = 1,
					//here since context.favouritesDataStore is called, migration of the other one will
					//already be performed, so no need to worry about how to notify it to migrate
					listSTM = context.favouritesDataStore.data.first()
						.listSTM.map {
							StmFavouriteBusItemDto(it.stopName, it.routeId, it.directionId, it.direction, it.lastStop)
						}.toPersistentList()
				)
			}

		}
	)}
)

