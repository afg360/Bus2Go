package dev.mainhq.bus2go.data.data_source.local.datastore.deprecated

import android.app.Application
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first

@Deprecated("Seems to be useless...")
object FavouritesDataStoreMigrations {

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
}