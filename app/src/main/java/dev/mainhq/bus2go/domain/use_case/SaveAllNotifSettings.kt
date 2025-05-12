package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.repository.SettingsRepository

class SaveAllNotifSettings(
	private val settingsRepository: SettingsRepository
) {

	operator fun invoke(appUpdateNotif: Boolean, dbUpdateNotif: Boolean): Boolean{
		return settingsRepository.saveAppUpdateNotifSetting(appUpdateNotif)
				&& settingsRepository.saveDbUpdateNotifSetting(dbUpdateNotif)
	}
}