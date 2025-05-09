package dev.mainhq.bus2go.data.backgroundtask

import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.mainhq.bus2go.data.worker.DatabaseDownloadManagerWorker
import dev.mainhq.bus2go.domain.backgroundtask.DatabaseDownloadScheduler
import dev.mainhq.bus2go.domain.entity.DbToDownload
import java.util.concurrent.TimeUnit

class DebugDatabaseDownloadSchedulerImpl(
	private val workManager: WorkManager
): DatabaseDownloadScheduler {

	override suspend fun scheduleDatabaseDownloadTask(dbToDownload: DbToDownload) {
		when (dbToDownload) {
			DbToDownload.ALL -> {
				enqueueTask("STM")
				enqueueTask("EXO")
			}

			DbToDownload.STM -> {
				enqueueTask("STM")
			}

			DbToDownload.EXO -> {
				enqueueTask("EXO")
			}
		}
	}

	private fun enqueueTask(key: String){
		workManager.enqueue(
			OneTimeWorkRequest.Builder(DatabaseDownloadManagerWorker::class.java)
				.addTag("DatabaseDownloadTask")
				.setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
				.setInputData(workDataOf(Pair(DatabaseDownloadManagerWorker.KEY, key)))
				.build()
		)
	}

}