package app.bus2go.android.domain.use_case.db_state

import app.bus2go.android.domain.entity.Time
import app.bus2go.android.domain.repository.AppStateRepository

class SetDatabaseExpirationDate(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(time: Time){
		appStateRepository.setNextDatabaseExpirationNotifDate(time)
	}
}