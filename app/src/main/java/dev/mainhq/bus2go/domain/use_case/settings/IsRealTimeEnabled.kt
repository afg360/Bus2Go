package dev.mainhq.bus2go.domain.use_case.settings

import dev.mainhq.bus2go.domain.repository.SettingsRepository

class IsRealTimeEnabled(
	private val settingsRepository: SettingsRepository
) {

	operator fun invoke(): Boolean{
		return settingsRepository.isRealTimeEnabled()
	}
}