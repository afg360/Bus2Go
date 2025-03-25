package dev.mainhq.bus2go.data.data_source.local.preference.app_state

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
	val DATABASES_STATE = stringPreferencesKey("databases_state")
	val IS_FIRST_TIME = booleanPreferencesKey("isFirstTime")
}

