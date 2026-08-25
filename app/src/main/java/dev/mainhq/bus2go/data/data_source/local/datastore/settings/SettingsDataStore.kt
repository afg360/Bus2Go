package dev.mainhq.bus2go.data.data_source.local.datastore.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
	name = "settings",
	produceMigrations = { context ->
		listOf()
	}
)

object SettingsDataStoreKeys {
	val LANGUAGE = stringPreferencesKey("language")
	val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")

	val SERVER = stringPreferencesKey("server")
	val IS_SELF_HOSTED = booleanPreferencesKey("is_self_hosted")
	val IS_REAL_TIME_ON = booleanPreferencesKey("is_real_time_on")

	val IS_APP_UPDATES_NOTIFS_ON = booleanPreferencesKey("is_app_updates_notifs_on")
	val IS_AUTOMATIC_UPDATES_ON = booleanPreferencesKey("is_automatic_updates_on")

	val IS_DATABASE_UPDATES_NOTIFS_ON = booleanPreferencesKey("is_database_updates_notifs_on")
	val IS_AUTOMATIC_DATABASE_UPDATES_ON = booleanPreferencesKey("is_automatic_database_updates_on")
}
