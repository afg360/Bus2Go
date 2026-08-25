package dev.mainhq.bus2go.data.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.domain.core.Bus2GoLocalDatabase
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.repository.DatabaseControllerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds


class DatabaseControllerRepositoryImpl(
	private val dataDir: File,
	private val cacheDir: File,
): DatabaseControllerRepository {

	private var databaseInstances: ConcurrentHashMap<DatabaseAgency, Bus2GoLocalDatabase> = ConcurrentHashMap()

	@Synchronized
	override fun createInstance(context: Context, databaseAgency: DatabaseAgency): Bus2GoLocalDatabase? {
		return databaseInstances[databaseAgency] ?: _createInstance(context, databaseAgency)?.also {
			databaseInstances[databaseAgency] = it
		}
	}

	private fun _createInstance(context: Context, databaseAgency: DatabaseAgency): Bus2GoLocalDatabase? {
		val databaseName = "${databaseAgency.toDatabaseFileNamePrefixString()}.db"
		val databasePath = "databases/$databaseName"
		val kclass = when(databaseAgency) {
			DatabaseAgency.STM -> AppDatabaseSTM::class.java
			DatabaseAgency.EXO -> AppDatabaseExo::class.java
		}

		val dbFile = context.getDatabasePath(databaseName)
		//FIXME this is a hack, better checks need to be performed to determine the correct
		// db to read
		try {
			return if (dbFile.exists() && dbFile.length() > 0) {
				Room.databaseBuilder(context, kclass, databaseName)
					.apply {
						when(databaseAgency) {
							DatabaseAgency.STM -> {
								addMigrations(AppDatabaseSTM.MIGRATION_1_2)
								addMigrations(AppDatabaseSTM.MIGRATION_2_3)
							}
							DatabaseAgency.EXO -> {
								addMigrations(AppDatabaseExo.MIGRATION_1_2)
							}
						}
					}
					.build()
			}
			//FIXME does it work even if bundled...?
			//FIXME database or databases???
			else if (context.assets.list("database")?.contains(databaseName) == true) {
				//from assets if bundled
				Room.databaseBuilder(context, kclass, databaseName)
					.createFromAsset(databasePath)
					.apply {
						when(databaseAgency) {
							DatabaseAgency.STM -> {
								addMigrations(AppDatabaseSTM.MIGRATION_1_2)
								addMigrations(AppDatabaseSTM.MIGRATION_2_3)
							}
							DatabaseAgency.EXO -> {
								addMigrations(AppDatabaseExo.MIGRATION_1_2)
							}
						}
					}
					.build()
			}
			else {
				null
			}
		}
		catch (_: IOException) {
			//FIXME shouldnt have that logcat here but is convenient @ the moment...
			Log.e("DATABASES", "$databaseName database not found...")
			return null
		}
	}


	@Synchronized
	override fun deleteDatabase(databaseAgency: DatabaseAgency): Boolean {
		//FIXME need to lock the map
		databaseInstances.remove(databaseAgency)?.also {  (it as RoomDatabase).close()  }

		//FIXME should perhaps instead be using ApplicationContext.closeDatabase + filesDir or dataDir???
		var allDeleted = true
		val dbDir = File(dataDir, "databases")
		if (!dbDir.exists()) {
			return true
		}

		//delete files living under "databases" folder, main files
		dbDir.listFiles { _, name ->
			name.startsWith(databaseAgency.toDatabaseFileNamePrefixString())
		}?.forEach { file ->
			if (file.exists()) {
				val deleted = file.delete()
				if (!deleted) {
					Log.e("DB_DELETE", "Failed to delete: ${file.name}")
					allDeleted = false
				}
			}
		}

		//delete lck file living inside the cache directory
		cacheDir.listFiles { _, name ->
			name.startsWith(databaseAgency.toDatabaseFileNamePrefixString()) && name.endsWith(".lck")
		}?.forEach { file ->
			if (file.exists()) {
				val deleted = file.delete()
				if (!deleted) {
					Log.e("DB_DELETE", "Failed to delete: ${file.name}")
					allDeleted = false
				}
			}
		}

		return allDeleted
	}

}