package dev.mainhq.bus2go.data.data_source.local.datastore.app_state

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.mainhq.bus2go.domain.entity.DbToDownload


val Context.appStateDataStore: DataStore<Preferences> by preferencesDataStore(
	name = "application_state",
	produceMigrations = { context ->
		listOf( )
	}
)

object AppStateDataStoreKeys {
	val NEXT_DATABASE_EXPIRATION_NOTIF_DATE = stringPreferencesKey("next_database_expiration_notif_date")
	val DATABASES_DIALOG_LAST_SHOWN_DATE = stringPreferencesKey("db_dialog_last_shown_date")
	val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
	//stores the saved version of the databases (NOT THE DATABASE SCHEMA VERSIONS!)
	val SQLITE_STM_VERSION = intPreferencesKey("sqlite_stm_version")
	val SQLITE_EXO_VERSION = intPreferencesKey("sqlite_exo_version")

	fun getDatabaseVersionPreference(db: DbToDownload): Preferences.Key<Int> {
		return when(db) {
			DbToDownload.STM -> SQLITE_STM_VERSION
			DbToDownload.EXO -> SQLITE_EXO_VERSION
		}
	}
}
