package dev.mainhq.bus2go.data.data_source.local.preference.deprecated

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import java.io.File

/** The datastore of favourites refers to favourites defined in the preferences file, at dev.mainhq.schedules.preferences,
 *  NOT THE Favourites.kt FRAGMENT */
val Context.favouritesDataStoreOld by dataStore(
	fileName = "favourites.json",
	serializer = SettingsSerializerOld,
)

val Context.favouritesDataStore by dataStore(
	fileName = "favourites_1.json",
	serializer = SettingsSerializer,
	produceMigrations = {
			context -> listOf(
		object : DataMigration<FavouritesData> {
			override suspend fun cleanUp() {
				val oldFile = File(context.filesDir.resolve("datastore"), "favourites.json")
				if (oldFile.exists()) {
					oldFile.delete()
				}
			}

			override suspend fun shouldMigrate(currentData: FavouritesData): Boolean {
				return File(context.filesDir.resolve("datastore"), "favourites.json").exists()
			}

			override suspend fun migrate(currentData: FavouritesData): FavouritesData {
				return currentData.copy(
					listSTM = context.favouritesDataStoreOld.data.first().listSTM,
					listExo = context.favouritesDataStoreOld.data.first().listExo.toList().map{
						ExoBusData(
							stopName = it.stopName,
							routeId = "",
							direction = it.direction,
							routeLongName = "",
							headsign = it.routeId
						)
					}.toPersistentList(),
					listExoTrain = context.favouritesDataStoreOld.data.first().listExoTrain
				)
			}

		}
	)
	}
)

suspend fun migrateFavouritesData(application: Application, updateData: List<Pair<String, String>>){
	//FIXME DO IT ONLY WHEN NEEDED
	application.favouritesDataStore.updateData { favouritesData ->
		favouritesData.copy(
			//recreate the list by initialising the data
			listExo = favouritesData.listExo.zip(updateData){ favourite, updatedData ->
				ExoBusData(favourite.stopName, updatedData.first, favourite.direction, updatedData.second, favourite.headsign)
			}.toPersistentList()
		)
	}
}

suspend fun doesNeedMigration(application: Application): Boolean {
	if (application.favouritesDataStore.data.first().listExo.isNotEmpty()){
		return application.favouritesDataStore.data.first().listExo.any { it.routeId == "" }
	}
	return false
}

