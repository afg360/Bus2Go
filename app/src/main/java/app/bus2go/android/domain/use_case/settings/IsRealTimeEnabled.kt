package app.bus2go.android.domain.use_case.settings

import app.bus2go.android.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class IsRealTimeEnabled(
	private val settingsRepository: SettingsRepository
) {

	operator fun invoke(): Flow<Boolean> {
		return settingsRepository.isRealTimeOn
	}
}