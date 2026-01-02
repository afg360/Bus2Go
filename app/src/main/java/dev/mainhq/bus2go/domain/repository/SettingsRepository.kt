package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.SettingsData

interface SettingsRepository {

	fun getSettings(): SettingsData

	//TODO more there...

	/** @return A boolean indicating success/failure */
	fun saveBus2GoServer(url: String): Boolean

	/** @param appUpdateNotif Boolean to save
	 * @return Success or failure of operation
	 */
	fun saveAppUpdateNotifSetting(appUpdateNotif: Boolean): Boolean

	/**
	 * @return Success or failure of operation
	 */
	fun saveDbUpdateNotifSetting(dbUpdateNotif: Boolean): Boolean
}