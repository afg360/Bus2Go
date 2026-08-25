package dev.mainhq.bus2go.domain.use_case.settings

import dev.mainhq.bus2go.domain.repository.SettingsRepository

class SetLang(
	private val settingsRepository: SettingsRepository
) {
	suspend operator fun invoke(langPos: Int) {
		settingsRepository.setLang(langPos)
	}
}