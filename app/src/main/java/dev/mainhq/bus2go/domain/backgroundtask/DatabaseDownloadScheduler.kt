package dev.mainhq.bus2go.domain.backgroundtask

import androidx.work.WorkInfo
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface DatabaseDownloadScheduler {

	suspend fun scheduleDatabaseDownloadTask(databaseAgency: DatabaseAgency): UUID

	suspend fun observeWork(workId: UUID): Flow<WorkInfo> //FIXME get rid of WorkInfo dependency (android based class)
}