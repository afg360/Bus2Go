package dev.mainhq.bus2go.domain.use_case.settings

import dev.mainhq.bus2go.domain.entity.SettingsData
import dev.mainhq.bus2go.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

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