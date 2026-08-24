package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.mainhq.bus2go.data.data_source.local.LocalKeyStore
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.data.data_source.local.datastore.app_state.AppStateDataStoreKeys
import dev.mainhq.bus2go.data.repository.DatabaseDownloadRepositoryAbstractImpl.Companion.COMPRESSION_EXT
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DatabaseState
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.repository.TransitRepository
import dev.mainhq.bus2go.utils.toLocalDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppStateRepositoryImpl(
	private val appStateDataStore: DataStore<Preferences>,
	private val localKeyStore: LocalKeyStore,
	private val databasesDir: File,
	private val filesDir: File,
	private val repos: List<TransitRepository>
): AppStateRepository {


	override suspend fun getNextDatabaseExpirationNotifDate(): Result<LocalDate> {
		return withContext(Dispatchers.IO) {
			appStateDataStore.data.first()[AppStateDataStoreKeys.NEXT_DATABASE_EXPIRATION_NOTIF_DATE]?.let{
				Result.Success(LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE))
			} ?: Result.Error(null)
		}
	}

	override suspend fun setNextDatabaseExpirationNotifDate(localDate: LocalDate) {
		withContext(Dispatchers.IO) {
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.NEXT_DATABASE_EXPIRATION_NOTIF_DATE] = localDate.toLocalDateString()
			}
		}
	}

	override suspend fun getGarbageFiles(): List<String> {
		return withContext(Dispatchers.IO) {
			//FIXME do we need dataDir or filesDir?
			val tmps = filesDir.list()?.filter { it.matches("\\.(te?mp|part)$".toRegex()) }?.filterNotNull() ?: listOf()
			val stmMaxVersion = getMaxVersion(DatabaseAgency.STM)
			val exoMaxVersion = getMaxVersion(DatabaseAgency.EXO)
			println("Stm: $stmMaxVersion, Exo: $exoMaxVersion")
			tmps + _getGarbageFiles(DatabaseAgency.STM, stmMaxVersion) + _getGarbageFiles(DatabaseAgency.EXO, exoMaxVersion)
		}
	}

	private fun getMaxVersion(databaseAgency: DatabaseAgency): Int {
		return filesDir.list()?.filter {
			it.matches("^(${databaseAgency.name.lowercase()})(_sample)?_data_[0-9]+\\.db\\.gz$".toRegex())
		}?.map {
			//we will be keeping database with the current version in case something has gone wrong...
			it.split("_").last().removeSuffix(".db.gz").toInt()
			//for if the list is empty (i.e. nothing downloaded yet), return null
		}?.maxByOrNull { it } ?: -1
	}

	private fun _getGarbageFiles(databaseAgency: DatabaseAgency, maxVersion: Int): List<String> {
		return filesDir.list()?.filter {
			it.matches("^(${databaseAgency.name.lowercase()})(_sample)?_data_[0-9]+\\.db\\.gz$".toRegex()) //(with \\d smaller than current version)
		}?.filter {
			it.split("_").last().removeSuffix(".db.gz").toInt() < maxVersion
		}?.filterNotNull() ?: listOf()
	}

	override suspend fun doesUpToDateCompressedDbExist(
		db: DatabaseAgency,
		version: Int,
	): String? {
		val dbNamePrefix = when(db){
			DatabaseAgency.STM -> AppDatabaseSTM.FILENAME_PREFIX
			DatabaseAgency.EXO -> AppDatabaseExo.FILENAME_PREFIX
		}
		//TODO before downloading, check if file exists already with the correct version
		//logger?.debug(TAG, "Looking for already downloaded databases")
		return filesDir.listFiles()
			?.find {
				it.name.matches("${dbNamePrefix}_${version}\\.db\\.${COMPRESSION_EXT}$".toRegex())
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
		return appStateDataStore.data.map { preferences ->
			preferences[AppStateDataStoreKeys.DATABASES_DIALOG_LAST_SHOWN_DATE]?.let { date ->
				Result.Success(LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE))
			} ?: Result.Error(null)
		}
	}

	override suspend fun setUpdateDbDialogLastShownDate(date: LocalDate) {
		withContext(Dispatchers.IO){
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.DATABASES_DIALOG_LAST_SHOWN_DATE] = date.toLocalDateString()
			}
		}
	}

	override suspend fun getDatabaseVersion(databaseAgency: DatabaseAgency): Int {
		return withContext(Dispatchers.IO){
			appStateDataStore.data.map { preferences ->
				val appStateDataStoreKey = when(databaseAgency) {
					DatabaseAgency.STM -> AppStateDataStoreKeys.SQLITE_STM_VERSION
					DatabaseAgency.EXO -> AppStateDataStoreKeys.SQLITE_EXO_VERSION
				}
				preferences[appStateDataStoreKey] ?: -1
			}.first()
		}
	}

	override suspend fun updateDatabaseVersion(databaseAgency: DatabaseAgency, version: Int) {
		//TODO return a Result to indicate in case version is smaller...?
		return withContext(Dispatchers.IO){
			appStateDataStore.edit { mutablePreferences ->
				val appStateDataStoreKey = when(databaseAgency) {
					DatabaseAgency.STM -> AppStateDataStoreKeys.SQLITE_STM_VERSION
					DatabaseAgency.EXO -> AppStateDataStoreKeys.SQLITE_EXO_VERSION
				}
				mutablePreferences[appStateDataStoreKey] = version
			}
		}
	}

	//TODO could instead have a private function that takes a map function, and the update database
	// function be that map function that way when it is called it mutates as we wish the correct
	// agency that is downloading
	override val databases: Flow<List<DatabaseState>> = flow {
		val list = mutableListOf<DatabaseState>()
		//Version number may exist, but during download some shit might have happened to cancel
		// download of the actual database, need to check for that edge case
		repos.forEach { repo ->
			val repoName = DatabaseAgency.getEntry(repo.dbName)
			when (val expirationDate = repo.getDatabaseExpirationDate()) {
				is Result.Error -> {
					//FIXMe doesnt work yet
					// this part may happen if the file doesn't exist, or when the file was just extracted
					// therefore need to check if the file exists in the dir
					databasesDir.listFiles()?.find { it.name.matches("${repoName.toString().lowercase()}_data".toRegex()) }?.also {
						list.add(DatabaseState.NeedAppRestart(repoName))
					} ?: list.add(DatabaseState.DatabaseNotDownloaded(repoName))
				}

				is Result.Success<LocalDate> -> {
					val sqliteVersion =
						appStateDataStore.data.first()[AppStateDataStoreKeys.getDatabaseVersionPreference(
							repoName
						)]
					if (sqliteVersion == null) {
						list.add(DatabaseState.DatabaseNotDownloaded(repoName))
					} else {
						list.add(
							DatabaseState.DatabaseDownloaded(
								DatabaseAgency.STM,
								sqliteVersion,
								expirationDate.data,
								//FIXME NEED A MORE RELIABLE WAY TO SETUP THIS STRING THAT DEPENDS
								// ON BACKEND HAVING CORRECT NAMING
								File(
									databasesDir,
									"${repoName.toString().lowercase()}_data.db"
								).length() / 1_000_000L
							)
						)
					}
				}
			}
		}
		emit(list)
	}

	override val databaseWorkNameState = appStateDataStore.data.map { preferences ->
		DatabaseAgency.entries.associateWith {
			preferences[booleanPreferencesKey("UNIQUE_WORK_NAME_${it}")] ?: false
		}
	}

	override suspend fun setRestartNeededFlag(databaseAgency: DatabaseAgency) {
		appStateDataStore.edit { mutablePreferences ->
			mutablePreferences[booleanPreferencesKey("UNIQUE_WORK_NAME_${databaseAgency}")] = true
		}
	}

	override suspend fun resetRestartNeededFlag() {
		appStateDataStore.edit { mutablePreferences ->
			DatabaseAgency.entries.forEach {
				mutablePreferences -= booleanPreferencesKey("UNIQUE_WORK_NAME_${it}")
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
				if (databasesDir.exists() && databasesDir.isDirectory){
					return@withContext databasesDir.list()?.isEmpty() ?: true
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

	override suspend fun setSelfSignedCert(cert: X509Certificate) {
		withContext(Dispatchers.IO) {
			localKeyStore.saveNewCertificate(cert)
		}
	}

	override suspend fun deleteDatabase(databaseAgency: DatabaseAgency) {
		withContext(Dispatchers.IO) {
			appStateDataStore.edit { mutablePreferences ->
				val appStateDataStoreKey = when(databaseAgency) {
					DatabaseAgency.STM -> AppStateDataStoreKeys.SQLITE_STM_VERSION
					DatabaseAgency.EXO -> AppStateDataStoreKeys.SQLITE_EXO_VERSION
				}
				mutablePreferences.remove(appStateDataStoreKey)
				//TODO if need be, update next expiration date notif date
			}
		}
	}
}