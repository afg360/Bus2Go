package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.repository.AppStateRepository

class CleanUpTask(
	private val appStateRepository: AppStateRepository
){
	suspend operator fun invoke() {
		val files = appStateRepository.getGarbageFiles()
	}
}