package dev.mainhq.bus2go.data.data_source.local.preference.stm

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoBusData
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.favouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.favouritesDataStoreOld
import dev.mainhq.bus2go.data.data_source.local.preference.stm.entity.StmFavouriteBusItem
import dev.mainhq.bus2go.data.data_source.local.preference.stm.entity.StmFavouritesData
import dev.mainhq.bus2go.data.data_source.local.preference.stm.entity.StmFavouritesDataSerializer
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import java.io.File


val Context.stmFavouritesDataStore by dataStore(
	fileName = "favourites_stm.json",
	serializer = StmFavouritesDataSerializer,
	produceMigrations = { context -> listOf(
		object : DataMigration<StmFavouritesData> {
			//TODO
			override suspend fun cleanUp() {
				val oldFile = File(context.filesDir.resolve("datastore"), "favourites.json")
				if (oldFile.exists()) {
					oldFile.delete()
				}
			}

			//TODO
			override suspend fun shouldMigrate(currentData: StmFavouritesData): Boolean {
				return File(context.filesDir.resolve("datastore"), "favourites.json").exists()
			}

			override suspend fun migrate(currentData: StmFavouritesData): StmFavouritesData {
				return currentData.copy(
					listSTM = context.favouritesDataStore.data.first()
						.listSTM.map {
							StmFavouriteBusItem(it.stopName, it.routeId, it.directionId, it.direction, it.lastStop)
						}.toPersistentList()
				)
			}

		}
	)}
)
