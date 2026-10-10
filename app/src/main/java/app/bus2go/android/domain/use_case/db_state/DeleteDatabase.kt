package app.bus2go.android.domain.use_case.db_state

import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.repository.AppStateRepository
import app.bus2go.android.domain.repository.DatabaseControllerRepository

class DeleteDatabase(
	private val databaseControllerRepository: DatabaseControllerRepository,
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(databaseAgency: DatabaseAgency) {
		databaseControllerRepository.deleteDatabase(databaseAgency)
		appStateRepository.deleteDatabase(databaseAgency)
	}
}