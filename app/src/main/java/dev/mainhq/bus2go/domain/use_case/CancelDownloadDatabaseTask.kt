package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.backgroundtask.DatabaseDownloadScheduler
import dev.mainhq.bus2go.domain.entity.DatabaseAgency

class CancelDownloadDatabaseTask(
	private val databaseDownloadScheduler: DatabaseDownloadScheduler
) {

	suspend operator fun invoke(databaseAgency: DatabaseAgency) {
		databaseDownloadScheduler.cancelDatabaseDownloadTask(databaseAgency.name.uppercase())
	}
}