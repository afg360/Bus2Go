package dev.mainhq.bus2go.domain.use_case.settings

import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.repository.SettingsRepository

class SaveAllNotifSettings(
	private val settingsRepository: SettingsRepository,
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(appUpdateNotif: Boolean, dbUpdateNotif: Boolean): Boolean{
		appStateRepository.setIsFirstTime()
		return settingsRepository.saveAppUpdateNotifSetting(appUpdateNotif)
				&& settingsRepository.saveDbUpdateNotifSetting(dbUpdateNotif)
	}
}