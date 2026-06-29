package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.repository.AppStateRepository

class CleanUpGarbageFiles(
	private val appStateRepository: AppStateRepository
){
	suspend operator fun invoke() {
		appStateRepository.getStmDatabaseVersion()
		appStateRepository.getExoDatabaseVersion()
		appStateRepository.getGarbageFiles().forEach { appStateRepository.deleteFile(it) }
	}
}