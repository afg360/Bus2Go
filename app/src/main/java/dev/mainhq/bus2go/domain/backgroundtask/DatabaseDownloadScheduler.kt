package dev.mainhq.bus2go.domain.backgroundtask

import androidx.work.WorkInfo
import dev.mainhq.bus2go.domain.entity.DbToDownload
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface DatabaseDownloadScheduler {

	suspend fun scheduleDatabaseDownloadTask(dbToDownload: DbToDownload): UUID

	suspend fun observeWork(workId: UUID): Flow<WorkInfo> //FIXME get rid of WorkInfo dependency (android based class)
}