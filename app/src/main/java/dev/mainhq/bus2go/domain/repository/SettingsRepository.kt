package dev.mainhq.bus2go.domain.repository

interface SettingsRepository {

	fun isRealTimeEnabled(): Boolean
	//TODO more there...
}