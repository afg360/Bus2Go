package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.data.data_source.local.datastore.app_state.AppStateDataStoreKeys
import dev.mainhq.bus2go.data.repository.DatabaseDownloadRepositoryImpl.Companion.COMPRESSION_EXT
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.entity.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.NumberFormatException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.NoSuchElementException

class AppStateRepositoryImpl(
	private val appStateDataStore: DataStore<Preferences>,
	private val dataDir: File,
	private val filesDir: File
): AppStateRepository {

	//FIXME use the result pattern for cleaner handling of IO errors

	override suspend fun getDatabaseExpirationDate(): Result<LocalDate> {
		return withContext(Dispatchers.IO) {
			appStateDataStore.data.first()[AppStateDataStoreKeys.DATABASES_EXPIRATION_DATE]?.let{
				Result.Success(LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE))
			} ?: Result.Error(null)
		}
	}

	override suspend fun setDatabaseExpirationDate(localDate: LocalDate){
		withContext(Dispatchers.IO) {
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.DATABASES_EXPIRATION_DATE] = Time.toLocalDateString(localDate)
			}
		}
	}

	override suspend fun getGarbageFiles(): List<String> {
		return withContext(Dispatchers.IO) {
			//TODO also check if
			"^(stm|exo)(_sample)?_data_\\d+.db.gz$".toRegex() //(with \\d smaller than current version)
			dataDir.list()?.filter {
				it.matches("\\.te?mp$".toRegex()) ||
				try {
					//we will be keeping database with the current version in case something has gone wrong...
					val dbVersion = it.split("_").last().removeSuffix(".db.gz").toInt()
					dbVersion < getExoDatabaseVersion() || dbVersion < getStmDatabaseVersion()
				}
				catch (e: NoSuchElementException){
					//TODO("Logging to see wtf has gone wrong...")
					false
				}
				catch (e: NumberFormatException){
					false
				}
			} ?: listOf()
		}
	}

	override suspend fun doesUpToDateCompressedDbExist(
		db: DbToDownload,
		version: Int,
	): String? {
		val dbNamePrefix = when(db){
			DbToDownload.STM -> AppDatabaseSTM.FILENAME_PREFIX
			DbToDownload.EXO -> AppDatabaseExo.FILENAME_PREFIX
		}
		//TODO before downloading, check if file exists already with the correct version
		//logger?.debug(TAG, "Looking for already downloaded databases")
		return filesDir.listFiles()
			?.find {
				it.name.matches("${dbNamePrefix}_${version}\\.db\\.${COMPRESSION_EXT}$"
					.toRegex())
			}?.name
	}

	override suspend fun deleteFile(filename: String){
		return withContext(Dispatchers.IO){
			val file = File(dataDir, filename)
			if (!file.exists()){
				throw IllegalArgumentException("File does not exist the files dir or is invalid")
			}
			file.delete()
		}
	}

	override suspend fun getDbUpdateDialogLastShownDate(): Result<LocalDate> {
		return withContext(Dispatchers.IO){
			appStateDataStore.data.first()[AppStateDataStoreKeys.DATABASES_DIALOG_LAST_SHOWN_DATE]?.let {
				Result.Success(LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE))
			} ?: Result.Error(null)
		}
	}

	override suspend fun setUpdateDbDialogLastShownDate(date: LocalDate) {
		withContext(Dispatchers.IO){
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.DATABASES_DIALOG_LAST_SHOWN_DATE] = Time.toLocalDateString(date)
			}
		}
	}

	override suspend fun getStmDatabaseVersion(): Int {
		return withContext(Dispatchers.IO){
			 appStateDataStore.data.map { preferences ->
				 preferences[AppStateDataStoreKeys.SQLITE_STM_VERSION] ?: -1
			 }.first()
		}
	}

	override suspend fun updateStmDatabaseVersion(version: Int) {
		//TODO return a Result to indicate in case version is smaller...?
		return withContext(Dispatchers.IO){
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.SQLITE_STM_VERSION] = version
			}
		}
	}

	override suspend fun getExoDatabaseVersion(): Int {
		return withContext(Dispatchers.IO){
			appStateDataStore.data.map { preferences ->
				preferences[AppStateDataStoreKeys.SQLITE_EXO_VERSION] ?: -1
			}.first()
		}
	}

	override suspend fun updateExoDatabaseVersion(version: Int) {
		return withContext(Dispatchers.IO){
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.SQLITE_EXO_VERSION] = version
			}
		}
	}

	/**
	 * To check if first time opening the app, check for the existence of the PreferenceManager field
	 * If false/doesn't exist, then first time.
	 * However, for long time users, check if the databases exist. If they don't, then we are sure
	 * it is their first time.
	 **/
	override suspend fun getIsFirstTime(): Boolean {
		return withContext(Dispatchers.IO){
			val isFirstTime = appStateDataStore.data.first()[AppStateDataStoreKeys.IS_FIRST_TIME]
			if  (isFirstTime == null){
				//TODO check for the existence of a bus2go database folder/files
				val directory = File(dataDir, "databases")
				if (directory.exists() && directory.isDirectory){
					return@withContext directory.list()?.isEmpty() ?: true
				}
				return@withContext true
			}
			return@withContext isFirstTime
		}
	}

	override suspend fun setIsNotFirstTime() {
		withContext(Dispatchers.IO){
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.IS_FIRST_TIME] = false
			}
		}
	}
}