package app.bus2go.android.domain.use_case

import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.repository.AppStateRepository

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