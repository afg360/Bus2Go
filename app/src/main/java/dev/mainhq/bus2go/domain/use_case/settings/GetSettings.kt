package dev.mainhq.bus2go.domain.use_case.settings

import dev.mainhq.bus2go.domain.entity.SettingsData
import dev.mainhq.bus2go.domain.repository.SettingsRepository

class GetSettings(
	private val settingsRepository: SettingsRepository
) {
	suspend operator  fun invoke(): SettingsData {
		return settingsRepository.getSettings()
	}
}