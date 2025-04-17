package dev.mainhq.bus2go.domain.backgroundtask

import dev.mainhq.bus2go.domain.entity.DbToDownload

interface DatabaseDownloadScheduler {

	suspend fun scheduleDatabaseDownloadTask(dbToDownload: DbToDownload)
}