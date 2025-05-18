package dev.mainhq.bus2go.data.data_source.local.datastore.app_state

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


val Context.appStateDataStore: DataStore<Preferences> by preferencesDataStore(
	name = "application_state",
	produceMigrations = { context ->
		listOf(
		)
	}
)

object AppStateDataStoreKeys{
	//TODO perhaps be replaced with SQLITE thingy
	val DATABASES_STATE = stringPreferencesKey("databases_state")
	val IS_FIRST_TIME = booleanPreferencesKey("isFirstTime")
	//stores the saved version of the databases (NOT THE DATABASE SCHEMA VERSIONS!)
	val SQLITE_STM_VERSION = intPreferencesKey("sqlite_stm_version")
	val SQLITE_EXO_VERSION = intPreferencesKey("sqlite_exo_version")
}
