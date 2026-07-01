package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.data.data_source.local.datastore.app_state.AppStateDataStoreKeys
import dev.mainhq.bus2go.data.repository.DatabaseDownloadRepositoryAbstractImpl.Companion.COMPRESSION_EXT
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.entity.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppStateRepositoryImpl(
	private val appStateDataStore: DataStore<Preferences>,
	private val dataDir: File,
	private val filesDir: File
): AppStateRepository {

	//FIXME use the result pattern for cleaner handling of IO errors
	override suspend fun getNextDatabaseExpirationNotifDate(): Result<LocalDate> {
		return withContext(Dispatchers.IO) {
			appStateDataStore.data.first()[AppStateDataStoreKeys.NEXT_DATABASE_EXPIRATION_NOTIF_DATE]?.let{
				Result.Success(LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE))
			} ?: Result.Error(null)
		}
	}

	//TODO
	override suspend fun setNextDatabaseExpirationNotifDate(localDate: LocalDate) {
		withContext(Dispatchers.IO) {
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.NEXT_DATABASE_EXPIRATION_NOTIF_DATE] = Time.toLocalDateString(localDate)
			}
		}
	}

	override suspend fun getGarbageFiles(): List<String> {
		return withContext(Dispatchers.IO) {
			//FIXME do we need dataDir or filesDir?
			val tmps = filesDir.list()?.filter { it.matches("\\.(te?mp|part)$".toRegex()) }?.filterNotNull() ?: listOf()
			val stmMaxVersion = getMaxVersion(DbToDownload.STM)
			val exoMaxVersion = getMaxVersion(DbToDownload.EXO)
			println("Stm: $stmMaxVersion, Exo: $exoMaxVersion")
			tmps + _getGarbageFiles(DbToDownload.STM, stmMaxVersion) + _getGarbageFiles(DbToDownload.EXO, exoMaxVersion)
		}
	}

	private fun getMaxVersion(dbToDownload: DbToDownload): Int {
		return filesDir.list()?.filter {
			it.matches("^(${dbToDownload.name.lowercase()})(_sample)?_data_[0-9]+\\.db\\.gz$".toRegex())
		}?.map {
			//we will be keeping database with the current version in case something has gone wrong...
			it.split("_").last().removeSuffix(".db.gz").toInt()
			//for if the list is empty (i.e. nothing downloaded yet), return null
		}?.maxByOrNull { it } ?: -1
	}

	private fun _getGarbageFiles(dbToDownload: DbToDownload, maxVersion: Int): List<String> {
		return filesDir.list()?.filter {
			it.matches("^(${dbToDownload.name.lowercase()})(_sample)?_data_[0-9]+\\.db\\.gz$".toRegex()) //(with \\d smaller than current version)
		}?.filter {
			it.split("_").last().removeSuffix(".db.gz").toInt() < maxVersion
		}?.filterNotNull() ?: listOf()
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
			val file = File(filesDir, filename)
			if (file.exists()){
				file.delete()
			}
			else {
				println("File ${file.name} does not exist in filesDir")
			}
		}
	}

	override fun getDbUpdateDialogLastShownDate(): Flow<Result<LocalDate>> {
		return appStateDataStore.data.map {
			val lastShownDate = it[AppStateDataStoreKeys.DATABASES_DIALOG_LAST_SHOWN_DATE]
				?: return@map Result.Error(null)
			Result.Success(LocalDate.parse(lastShownDate, DateTimeFormatter.BASIC_ISO_DATE))
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