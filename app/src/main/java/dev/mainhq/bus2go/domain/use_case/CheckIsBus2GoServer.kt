package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository

class CheckIsBus2GoServer(
	private val databaseDownloadRepository: DatabaseDownloadRepository
) {
	suspend operator fun invoke(str: String): Result<Boolean> {
		return databaseDownloadRepository.testIsBus2Go()
	}
}