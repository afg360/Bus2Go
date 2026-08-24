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
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit

class DatabaseDownloadSchedulerImpl(
	private val workManager: WorkManager
): DatabaseDownloadScheduler {

	private companion object {
		fun uniqueWorkName(databaseAgencyName: String): String {
			return "unique_${databaseAgencyName}_download"
		}
	}

	override suspend fun scheduleDatabaseDownloadTask(databaseAgencyName: String): UUID {
		return enqueueTask(databaseAgencyName)
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
uniqueWorkName(key),
			ExistingWorkPolicy.KEEP,
			workRequest
		)
		return workRequest.id
	}

	override fun observeWork(databaseAgencyName: String) : Flow<NotificationType.DbOperation?> =
		workManager.getWorkInfosForUniqueWorkFlow(uniqueWorkName(databaseAgencyName)).map { list ->
			try {
				list?.last()?.let {
					when (it.state) {
						WorkInfo.State.ENQUEUED -> NotificationType.DbEnqueued
						WorkInfo.State.RUNNING -> {
							if (it.progress.getBoolean(DatabaseDownloadManagerWorker.WORK_PROGRESS_IS_DECOMPRESSING, false)) {
								NotificationType.DbExtracting
							}
							else {
								NotificationType.DbDownloading(
									it.progress.getInt(
										DatabaseDownloadManagerWorker.WORK_PROGRESS_CURRENT,
										0
									),
									it.progress.getInt(
										DatabaseDownloadManagerWorker.WORK_PROGRESS_MAX,
										100
									),
								)
							}
						}

						WorkInfo.State.SUCCEEDED -> NotificationType.DbUpdateDone
						WorkInfo.State.FAILED -> NotificationType.DbUpdateError()
						WorkInfo.State.BLOCKED -> NotificationType.DbEnqueued
						WorkInfo.State.CANCELLED -> NotificationType.DbUpdateError()
					}
				}
			}
			catch (_: NoSuchElementException) {
				null
			}
	}
}