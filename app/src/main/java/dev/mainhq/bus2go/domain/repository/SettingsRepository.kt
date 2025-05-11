package dev.mainhq.bus2go.domain.repository

interface SettingsRepository {

	fun isRealTimeEnabled(): Boolean
	//TODO more there...

	/** @return A boolean indicating success/failure */
	fun saveBus2GoServer(url: String): Boolean
}