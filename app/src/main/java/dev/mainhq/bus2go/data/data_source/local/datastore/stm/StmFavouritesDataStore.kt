package dev.mainhq.bus2go.data.data_source.local.datastore.stm

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.StmFavouritesDataSerializer1
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataSerializer
import kotlinx.collections.immutable.toPersistentList
import java.io.File

val Context.stmFavouritesDataStore by dataStore(
	fileName = "favourites_stm_v2.json",
	serializer = StmFavouritesDataSerializer,
	produceMigrations = { context -> listOf(
		object : DataMigration<StmFavouritesDataDto> {
			override suspend fun cleanUp() {
				File(context.filesDir
					.resolve("datastore"), "favourites_stm.json")
					.apply {
						if (exists()) {
							delete()
						}
					}
			}

			override suspend fun shouldMigrate(currentData: StmFavouritesDataDto): Boolean {
				return File(
					context.filesDir.resolve("datastore"),
					"favourites_stm.json"
				).exists()
			}

			override suspend fun migrate(currentData: StmFavouritesDataDto): StmFavouritesDataDto {
				val oldSerializer = StmFavouritesDataSerializer1
				val oldFile = File(context.filesDir.resolve("datastore"), "favourites_stm.json")

				if (!oldFile.exists()){
					return currentData
				}

				val dataV1 = oldFile.inputStream().use {
					oldSerializer.readFrom(it)
				}

				return StmFavouritesDataDto(
					version = 2,
					listSTM = dataV1.listSTM.map {
						StmFavouriteBusItemDto(
							it.stopName,
							it.routeId,
							it.direction,
							listOf(),
							it.directionId,
							it.lastStop,
						)
					}.toPersistentList()
				)
			}
		}
	)}
)
