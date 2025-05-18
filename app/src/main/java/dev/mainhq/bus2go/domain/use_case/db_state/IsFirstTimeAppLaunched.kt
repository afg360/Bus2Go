package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.repository.AppStateRepository

class IsFirstTimeAppLaunched(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(): Boolean{
		return appStateRepository.getIsFirstTime()
	}
}