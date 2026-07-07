package dev.mainhq.bus2go.domain.use_case.settings

import dev.mainhq.bus2go.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class IsRealTimeEnabled(
	private val settingsRepository: SettingsRepository
) {

	operator fun invoke(): Flow<Boolean> {
		return settingsRepository.isRealTimeOn
	}
}