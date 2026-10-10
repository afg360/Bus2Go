package app.bus2go.android.domain.use_case.settings

import app.bus2go.android.domain.repository.AppStateRepository
import app.bus2go.android.domain.repository.SettingsRepository

class SaveAllNotifSettings(
	private val settingsRepository: SettingsRepository,
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(appUpdateNotif: Boolean, dbUpdateNotif: Boolean) {
		appStateRepository.setIsNotFirstTime()
		settingsRepository.setAppUpdatesNotif(appUpdateNotif)
		settingsRepository.setDbUpdatesNotif(dbUpdateNotif)
	}

	/** Default behaviour is to set every notif setting to false */
	suspend operator fun invoke() {
		appStateRepository.setIsNotFirstTime()
		settingsRepository.setAppUpdatesNotif(false)
		settingsRepository.setDbUpdatesNotif(false)
	}
}