package app.bus2go.android.data.repository

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import app.bus2go.android.data.data_source.local.database.exo.AppDatabaseExo
import app.bus2go.android.data.data_source.local.database.stm.AppDatabaseSTM
import app.bus2go.android.domain.core.Bus2GoLocalDatabase
import app.bus2go.android.domain.core.Logger
import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.repository.DatabaseControllerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap


//FIXME this is a fragile design using our application class and app module which is supposed to be
class DatabaseControllerRepositoryImpl private constructor(
	private val applicationContext: Context,
	private val loggerImpl: Logger?
): DatabaseControllerRepository {

	private val dataDir = applicationContext.dataDir
	private val cacheDir = applicationContext.cacheDir
	private var databaseInstances = ConcurrentHashMap<DatabaseAgency, MutableStateFlow<Bus2GoLocalDatabase?>>()

	companion object {

		private var INSTANCE: DatabaseControllerRepositoryImpl? = null

		@Synchronized
		fun getInstance(applicationContext: Context, loggerImpl: Logger?): DatabaseControllerRepositoryImpl {
			return INSTANCE ?: DatabaseControllerRepositoryImpl(applicationContext, loggerImpl).also { INSTANCE = it }
		}
	}

	@Synchronized
	override fun getDatabaseFlow(databaseAgency: DatabaseAgency): StateFlow<Bus2GoLocalDatabase?> {
		return databaseInstances[databaseAgency] ?: MutableStateFlow(createInstance(databaseAgency)).also {
			databaseInstances[databaseAgency] = it
		}.asStateFlow()
	}

	/* Is fine to create an instance after having deleted the file, since the db returned will be null */
	private fun createInstance(databaseAgency: DatabaseAgency): Bus2GoLocalDatabase? {
		val databaseName = "${databaseAgency.toDatabaseFileNamePrefixString()}.db"
		val databasePath = "databases/$databaseName"
		val kclass = when(databaseAgency) {
			DatabaseAgency.STM -> AppDatabaseSTM::class.java
			DatabaseAgency.EXO -> AppDatabaseExo::class.java
		}

		val dbFile = applicationContext.getDatabasePath(databaseName)
		//FIXME this is a hack, better checks need to be performed to determine the correct
		// db to read
		try {
			return if (dbFile.exists() && dbFile.length() > 0) {
				Room.databaseBuilder(applicationContext, kclass, databaseName)
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
			else if (applicationContext.assets.list("database")?.contains(databaseName) == true) {
				//from assets if bundled
				Room.databaseBuilder(applicationContext, kclass, databaseName)
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
			loggerImpl?.error("DATABASES", "$databaseName database not found...")
			return null
		}
	}


	@Synchronized
	override fun deleteDatabase(databaseAgency: DatabaseAgency): Boolean {
		databaseInstances[databaseAgency]?.also { stateFlow ->
			stateFlow.update {
				(it as RoomDatabase?)?.close()
				null
			}
		}

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
					loggerImpl?.error("DB_DELETE", "Failed to delete: ${file.name}")
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
					loggerImpl?.error("DB_DELETE", "Failed to delete: ${file.name}")
					allDeleted = false
				}
			}
		}

		return allDeleted
	}

}