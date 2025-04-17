package dev.mainhq.bus2go.data.repository

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.mainhq.bus2go.data.worker.DebugDatabaseDownloadManagerWorker
import dev.mainhq.bus2go.domain.repository.DbDownloadRepository

class DebugDbDownloadRepositoryImpl(
	private val workManager: WorkManager
): DbDownloadRepository  {


	override suspend fun downloadAllDb() {
		TODO("Not yet implemented")
	}

	override suspend fun downloadStmDb() {
		val data = workDataOf(Pair("stm", true), Pair("exo", false))
		workManager.beginWith(
				OneTimeWorkRequest.Builder(DebugDatabaseDownloadManagerWorker::class.java)
				.addTag("StmDbDownloadTask")
				.setInputData(data)
				.build()
			).enqueue()
	}

	override suspend fun downloadExoDb() {
		TODO("Not yet implemented")
	}
}