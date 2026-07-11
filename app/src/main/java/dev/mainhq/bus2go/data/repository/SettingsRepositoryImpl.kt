package dev.mainhq.bus2go.data.repository

import android.content.Context
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.data_source.local.datastore.settings.SettingsDataStoreKeys
import dev.mainhq.bus2go.data.data_source.local.datastore.settings.settingsDataStore
import dev.mainhq.bus2go.domain.entity.ServerChoice
import dev.mainhq.bus2go.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
	private val appContext: Context
): SettingsRepository {

	override val lang: Flow<Int>
		/**
		 * @return Returns -1 if no item in the array with the saved data
		 * */
		get() {
			return appContext.settingsDataStore.data.map {
				getLangResId(it[SettingsDataStoreKeys.SERVER])
			}
		}

	override suspend fun setLang(langPos: Int) {
		withContext(Dispatchers.IO) {
			appContext.settingsDataStore.updateData {
				it.toMutablePreferences().toMutablePreferences().apply {
					val langsArray = appContext.resources.getStringArray(R.array.langs)
					if (langPos > langsArray.size) {
						set(SettingsDataStoreKeys.LANGUAGE, "System")
					}
					else {
						set(SettingsDataStoreKeys.LANGUAGE, langsArray[langPos])
					}
				}
			}
		}
	}

	override val isDarkMode: Flow<Boolean>
		get() {
			return appContext.settingsDataStore.data.map {
				it[SettingsDataStoreKeys.IS_DARK_MODE] ?: true
			}
		}

	override suspend fun setIsDarkMode(isDarkMode: Boolean) {
		withContext(Dispatchers.IO) {
			appContext.settingsDataStore.updateData {
				it.toMutablePreferences().toMutablePreferences().apply {
					set(SettingsDataStoreKeys.IS_DARK_MODE, isDarkMode)
				}
			}
		}
	}

	override val serverChoice: Flow<ServerChoice>
		get() {
			return appContext.settingsDataStore.data.map {
				ServerChoice(
					it[SettingsDataStoreKeys.SERVER] ?: "",
					it[SettingsDataStoreKeys.IS_SELF_HOSTED] ?: true
				)
			}
		}

	override suspend fun setBus2GoServer(url: String) {
		withContext(Dispatchers.IO) {
			appContext.settingsDataStore.updateData {
				it.toMutablePreferences().apply {
					set(SettingsDataStoreKeys.SERVER, url)
				}
			}
		}
	}

	override suspend fun toggleIsSelfHosted() {
		withContext(Dispatchers.IO) {
			appContext.settingsDataStore.updateData {
				it.toMutablePreferences().apply {
					set(SettingsDataStoreKeys.IS_SELF_HOSTED, get(SettingsDataStoreKeys.IS_SELF_HOSTED)?.not() ?: true)
				}
			}
		}
	}

	override val isRealTimeOn: Flow<Boolean>
		get() {
			return appContext.settingsDataStore.data.map {
				it[SettingsDataStoreKeys.IS_REAL_TIME_ON] ?: false
			}
		}

	override suspend fun saveAppUpdateNotifSetting(appUpdateNotif: Boolean) {
		withContext(Dispatchers.IO) {
			appContext.settingsDataStore.updateData {
				it.toMutablePreferences().apply {
					set(SettingsDataStoreKeys.IS_APP_UPDATES_NOTIFS_ON, appUpdateNotif)
				}
			}
		}
	}

	override suspend fun saveDbUpdateNotifSetting(dbUpdateNotif: Boolean) {
		withContext(Dispatchers.IO) {
			appContext.settingsDataStore.updateData {
				it.toMutablePreferences().apply {
					set(SettingsDataStoreKeys.IS_DATABASE_UPDATES_NOTIFS_ON, dbUpdateNotif)
				}
			}
		}
	}

	private fun getLangResId(langStr: String?): Int {
		return appContext.resources.getStringArray(R.array.langs)
			.indexOf(langStr ?: "System").let {
				if (it < 0) {
					0
				}
				else {
					it
				}
			}
	}
}