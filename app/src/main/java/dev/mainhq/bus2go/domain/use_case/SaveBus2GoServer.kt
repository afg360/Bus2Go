package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.repository.SettingsRepository

class SaveBus2GoServer(
	private val settingsRepository: SettingsRepository
) {

	operator fun invoke(url: String): Boolean{
		return settingsRepository.saveBus2GoServer(url)
	}
}