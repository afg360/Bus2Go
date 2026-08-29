package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.repository.DatabaseControllerRepository

class DeleteDatabase(
	private val databaseControllerRepository: DatabaseControllerRepository,
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(databaseAgency: DatabaseAgency) {
		databaseControllerRepository.deleteDatabase(databaseAgency)
		appStateRepository.deleteDatabase(databaseAgency)
	}
}