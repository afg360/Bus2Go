package dev.mainhq.bus2go.domain.use_case.settings

import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.repository.SettingsRepository

class SaveAllNotifSettings(
	private val settingsRepository: SettingsRepository,
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(appUpdateNotif: Boolean, dbUpdateNotif: Boolean): Boolean {
		appStateRepository.setIsNotFirstTime()
		return settingsRepository.saveAppUpdateNotifSetting(appUpdateNotif)
				&& settingsRepository.saveDbUpdateNotifSetting(dbUpdateNotif)
	}

	/** Default behaviour is to set every notif setting to false */
	suspend operator fun invoke(): Boolean {
		appStateRepository.setIsNotFirstTime()
		return settingsRepository.saveAppUpdateNotifSetting(false)
				&& settingsRepository.saveDbUpdateNotifSetting(false)
	}
}