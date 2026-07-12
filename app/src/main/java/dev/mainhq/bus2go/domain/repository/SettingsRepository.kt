package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.ServerChoice
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

	val isAppUpdatesNotifOn: Flow<Boolean>
	suspend fun setAppUpdatesNotif(appUpdateNotif: Boolean)

	val isAutoAppUpdatesOn: Flow<Boolean>
	suspend fun setAutoAppUpdates(isAutoAppUpdate: Boolean)

	val isDbUpdatesNotifOn: Flow<Boolean>
	suspend fun setDbUpdatesNotif(dbUpdateNotif: Boolean)

	val isDbAutoUpdatesOn: Flow<Boolean>
	suspend fun setDbAutoUpdates(isAutoDbUpdate: Boolean)
}