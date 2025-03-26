package dev.mainhq.bus2go.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import dev.mainhq.bus2go.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
	//TODO is this alright to store??
	private val appContext: Context
): SettingsRepository {

	override fun isRealTimeEnabled(): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(appContext)
				.getBoolean("real-time-data", false)
	}
}