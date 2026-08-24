package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.repository.AppStateRepository

class CleanUpGarbageFiles(
	private val appStateRepository: AppStateRepository
){
	suspend operator fun invoke() {
		//TODO
		appStateRepository.getDatabaseVersion(DatabaseAgency.STM)
		appStateRepository.getDatabaseVersion(DatabaseAgency.EXO)
		appStateRepository.getGarbageFiles().forEach { appStateRepository.deleteFile(it) }
	}
}