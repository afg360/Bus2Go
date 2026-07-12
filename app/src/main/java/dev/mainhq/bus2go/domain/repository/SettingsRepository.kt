package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.ServerChoice
import dev.mainhq.bus2go.domain.entity.SettingsData
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

	val lang: Flow<Int>
	suspend fun setLang(langPos: Int)

	val isDarkMode: Flow<Boolean>
	suspend fun setIsDarkMode(isDarkMode: Boolean)
	//TODO more there...

	val serverChoice: Flow<ServerChoice>
	suspend fun setBus2GoServer(serverChoice: ServerChoice)
	suspend fun toggleIsSelfHosted()

	val isRealTimeOn: Flow<Boolean>

	suspend fun saveAppUpdateNotifSetting(appUpdateNotif: Boolean)

	suspend fun saveDbUpdateNotifSetting(dbUpdateNotif: Boolean)
}