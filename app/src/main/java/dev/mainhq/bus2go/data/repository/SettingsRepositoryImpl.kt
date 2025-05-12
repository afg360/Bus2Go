package dev.mainhq.bus2go.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
	//TODO is this alright to store??
	private val appContext: Context
): SettingsRepository {

	override fun isRealTimeEnabled(): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(appContext)
				.getBoolean("real-time-data", false)
	}

	override fun saveBus2GoServer(url: String): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(appContext).edit()
			.putString("server-choice", url)
			.commit()
	}

	override fun saveAppUpdateNotifSetting(appUpdateNotif: Boolean): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(appContext).edit()
			.putBoolean("update-notifications", appUpdateNotif)
			.commit()
	}

	override fun saveDbUpdateNotifSetting(dbUpdateNotif: Boolean): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(appContext).edit()
			.putBoolean("db-update-notifications", dbUpdateNotif)
			.commit()
	}
}