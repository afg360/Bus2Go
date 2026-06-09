package dev.mainhq.bus2go.data.data_source.local.datastore.exo

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.ExoFavouritesDataSerializer_v1
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteTrainItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataSerializer
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import java.io.File

//TODO
val Context.exoFavouritesDataStore by dataStore(
	fileName = "favourites_exo_v2.json",
	serializer = ExoFavouritesDataSerializer,
	produceMigrations = { context -> listOf(
		object : DataMigration<ExoFavouritesDataDto> {
			override suspend fun cleanUp() {
				//MigrationObserver.notifyExoCleanUpReady(context)
				File(context.filesDir.resolve("datastore"), "favourites_exo.json")
					.apply {
						if (exists()){
							delete()
						}
					}
			}

			override suspend fun shouldMigrate(currentData: ExoFavouritesDataDto): Boolean {
				return File(context.filesDir.resolve("datastore"), "favourites_exo.json").exists()
			}

			override suspend fun migrate(currentData: ExoFavouritesDataDto): ExoFavouritesDataDto {
				val oldFile = File(context.filesDir.resolve("datastore"), "favourites_exo.json")
				if (!oldFile.exists()) {
					return currentData
				}

				val oldData = oldFile.inputStream().use { input ->
					ExoFavouritesDataSerializer_v1.readFrom(input)
				}
				return ExoFavouritesDataDto(
					version = 2,
					listExo = oldData.listExo.mapIndexed { index, item ->
						ExoFavouriteBusItemDto(
							position = index,
							stopName = item.stopName,
							routeId = item.routeId,
							direction = item.direction,
							tags = persistentListOf(),
							routeLongName = item.routeLongName
						)
					}.toPersistentList(),
					listExoTrain = oldData.listExoTrain.mapIndexed { index, item ->
						ExoFavouriteTrainItemDto(
							position = index,
							stopName = item.stopName,
							routeId = item.routeId,
							direction = item.direction,
							tags = persistentListOf(),
							trainNum = item.trainNum,
							routeName = item.routeName,
							directionId = item.directionId
						)
					}.toPersistentList()
				)
			}

		}
	)}
)
