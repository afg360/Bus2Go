package dev.mainhq.bus2go.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.SettingsData
import dev.mainhq.bus2go.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
	private val appContext: Context
): SettingsRepository {

	companion object {
		private const val LANGUAGE = "language"
		private const val DARK_MODE = "dark-mode"
		private const val SERVER_CHOICE = "server-choice"
		private const val REAL_TIME_DATA = "real-time-data"
		private const val UPDATE_NOTIF = "update-notification"
		private const val DB_UPDATE_NOTIF = "db-update-notifications"
	}

	override fun getSettings(): SettingsData {
		val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
		return SettingsData(
			language = prefs.getString(LANGUAGE, "System") ?: "System",
			isDarkMode = prefs.getBoolean(DARK_MODE, true),
			serverChoice = prefs.getString(SERVER_CHOICE, "") ?: "",
			isRealTime = prefs.getBoolean(REAL_TIME_DATA, false)
		)
	}

	override fun saveBus2GoServer(url: String): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(appContext).edit()
			.putString(SERVER_CHOICE, url)
			.commit()
	}

	override fun saveAppUpdateNotifSetting(appUpdateNotif: Boolean): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(appContext).edit()
			.putBoolean(UPDATE_NOTIF, appUpdateNotif)
			.commit()
	}

	override fun saveDbUpdateNotifSetting(dbUpdateNotif: Boolean): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(appContext).edit()
			.putBoolean(DB_UPDATE_NOTIF, dbUpdateNotif)
			.commit()
	}
}