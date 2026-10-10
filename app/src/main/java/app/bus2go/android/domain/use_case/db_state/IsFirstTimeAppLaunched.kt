package app.bus2go.android.domain.use_case.db_state

import app.bus2go.android.domain.repository.AppStateRepository

class IsFirstTimeAppLaunched(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(): Boolean{
		return appStateRepository.getIsFirstTime()
	}
}