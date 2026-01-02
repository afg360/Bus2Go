package dev.mainhq.bus2go.domain.use_case

import androidx.work.WorkInfo
import dev.mainhq.bus2go.domain.backgroundtask.DatabaseDownloadScheduler
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ObserveDownloadDatabaseTask(
	private val databaseDownloadScheduler: DatabaseDownloadScheduler
){

	suspend operator fun invoke(workId: UUID): Flow<WorkInfo> {
		return databaseDownloadScheduler.observeWork(workId)
	}
}