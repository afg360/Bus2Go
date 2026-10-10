package app.bus2go.android.domain.use_case.settings

import app.bus2go.android.domain.entity.SettingsData
import app.bus2go.android.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetSettings(
	private val settingsRepository: SettingsRepository
) {

	operator fun invoke(): Flow<SettingsData> {
		return combine(
			settingsRepository.lang,
			settingsRepository.isDarkMode,
			settingsRepository.serverChoice,
			settingsRepository.isRealTimeOn
		) { language, isDarkMode, serverChoice, isRealTimeOn ->
			SettingsData(
				language,
				isDarkMode,
				serverChoice,
				isRealTimeOn
			)
		}
	}
}