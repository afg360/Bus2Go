package dev.mainhq.bus2go.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.mainhq.bus2go.data.data_source.local.datastore.app_state.AppStateDataStoreKeys
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.entity.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppStateRepositoryImpl(
	private val appStateDataStore: DataStore<Preferences>,
	private val dataDir: File
): AppStateRepository {

	//FIXME use the result pattern for cleaner handling of IO errors

	override suspend fun getDatabaseState(): LocalDate? {
		return withContext(Dispatchers.IO) {
			appStateDataStore.data.first()[AppStateDataStoreKeys.DATABASES_STATE]?.let{
				LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE)
			}
		}
	}

	override suspend fun setDatabaseState(localDate: LocalDate){
		withContext(Dispatchers.IO) {
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.DATABASES_STATE] = Time.toLocalDateString(localDate)
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

	override suspend fun getExoDatabaseVersion(): Int {
		return withContext(Dispatchers.IO){
			appStateDataStore.data.map { preferences ->
				preferences[AppStateDataStoreKeys.SQLITE_EXO_VERSION] ?: -1
			}.first()
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

	override suspend fun setIsFirstTime() {
		withContext(Dispatchers.IO){
			appStateDataStore.edit { mutablePreferences ->
				mutablePreferences[AppStateDataStoreKeys.IS_FIRST_TIME] = false
			}
		}
	}
}