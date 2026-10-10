package app.bus2go.android.domain.use_case.settings

import app.bus2go.android.domain.repository.SettingsRepository

class SetLang(
	private val settingsRepository: SettingsRepository
) {
	suspend operator fun invoke(langPos: Int) {
		settingsRepository.setLang(langPos)
	}
}