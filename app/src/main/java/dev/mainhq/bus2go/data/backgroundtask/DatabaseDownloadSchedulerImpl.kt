package dev.mainhq.bus2go.data.backgroundtask

import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.mainhq.bus2go.data.worker.DatabaseDownloadManagerWorker
import dev.mainhq.bus2go.domain.backgroundtask.DatabaseDownloadScheduler
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

class DatabaseDownloadSchedulerImpl(
	private val workManager: WorkManager
): DatabaseDownloadScheduler {

	override suspend fun scheduleDatabaseDownloadTask(databaseAgency: DatabaseAgency): UUID {
		return enqueueTask(databaseAgency.name.uppercase())
	}

	private fun enqueueTask(key: String): UUID {
		val workRequest = OneTimeWorkRequestBuilder<DatabaseDownloadManagerWorker>()
			.addTag("DatabaseDownloadTask")
			.setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
			.setInputData(workDataOf(DatabaseDownloadManagerWorker.KEY to key))
			//could be using a constraint for the network type, but custom logic seems better...
			//.setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
			.build()
		workManager.enqueueUniqueWork(
			"unique_${key}_download",
			ExistingWorkPolicy.KEEP,
			workRequest
		)
		return workRequest.id
	}

	override suspend fun observeWork(workId: UUID): Flow<WorkInfo> = workManager.getWorkInfoByIdFlow(workId)
}