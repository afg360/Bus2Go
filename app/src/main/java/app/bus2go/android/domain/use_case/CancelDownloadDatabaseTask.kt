package app.bus2go.android.domain.use_case

import app.bus2go.android.domain.backgroundtask.DatabaseDownloadScheduler
import app.bus2go.android.domain.entity.DatabaseAgency

class CancelDownloadDatabaseTask(
	private val databaseDownloadScheduler: DatabaseDownloadScheduler
) {

	suspend operator fun invoke(databaseAgency: DatabaseAgency) {
		databaseDownloadScheduler.cancelDatabaseDownloadTask(databaseAgency.name.uppercase())
	}
}