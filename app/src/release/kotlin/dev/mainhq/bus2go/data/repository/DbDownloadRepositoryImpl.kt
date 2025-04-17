package dev.mainhq.bus2go.data.repository

import androidx.work.WorkManager
import dev.mainhq.bus2go.domain.repository.DbDownloadRepository

class DbDownloadRepositoryImpl(
	private val workManager: WorkManager
): DbDownloadRepository {
	override suspend fun downloadAllDb() {
		TODO("Not yet implemented")
	}

	override suspend fun downloadStmDb() {
		TODO("Not yet implemented")
	}

	override suspend fun downloadExoDb() {
		TODO("Not yet implemented")
	}
}