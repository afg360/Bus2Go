package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.backgroundtask.DatabaseDownloadScheduler
import java.util.UUID

class ScheduleDownloadDatabaseTask(
	private val databaseDownloadScheduler: DatabaseDownloadScheduler,
) {

	//TODO perhaps return some sort of success or failure thing
	suspend operator fun invoke(dbsToDownload: List<DbToDownload>): List<UUID> {
		return dbsToDownload.map {
			databaseDownloadScheduler.scheduleDatabaseDownloadTask(it)
		}
	}
}