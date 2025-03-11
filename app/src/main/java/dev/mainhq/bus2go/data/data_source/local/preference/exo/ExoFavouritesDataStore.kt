package dev.mainhq.bus2go.data.data_source.local.preference.exo

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.favouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.preference.exo.entity.ExoFavouriteBusItem
import dev.mainhq.bus2go.data.data_source.local.preference.exo.entity.ExoFavouriteTrainItem
import dev.mainhq.bus2go.data.data_source.local.preference.exo.entity.ExoFavouritesData
import dev.mainhq.bus2go.data.data_source.local.preference.exo.entity.ExoFavouritesDataSerializer
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import java.io.File

//TODO BE SURE TO MIGRATE FROM V1 TO V2 BEFORE DOING THIS MIGRATION

val Context.exoFavouritesDataStore by dataStore(
	fileName = "favourites_exo.json",
	serializer = ExoFavouritesDataSerializer,
	produceMigrations = { context -> listOf(
		object : DataMigration<ExoFavouritesData> {
			//TODO
			override suspend fun cleanUp() {
				val oldFile = File(context.filesDir.resolve("datastore"), "favourites_1.json")
				if (oldFile.exists()) {
					oldFile.delete()
				}
			}

			//TODO
			override suspend fun shouldMigrate(currentData: ExoFavouritesData): Boolean {
				return File(context.filesDir.resolve("datastore"), "favourites_1.json").exists()
			}

			override suspend fun migrate(currentData: ExoFavouritesData): ExoFavouritesData {
				return currentData.copy(
					listExo = context.favouritesDataStore.data.first()
						.listExo.map {
							ExoFavouriteBusItem(it.stopName, it.routeId, it.direction, it.routeLongName, it.headsign)
						}.toPersistentList(),
					listExoTrain = context.favouritesDataStore.data.first()
						.listExoTrain.map {
							ExoFavouriteTrainItem(it.stopName, it.routeId, it.trainNum, it.routeName, it.directionId, it.direction)
						}.toPersistentList()
				)
			}

		}
	)}
)
