package app.bus2go.android.domain.use_case

import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.backgroundtask.DatabaseDownloadScheduler
import java.util.UUID

class ScheduleDownloadDatabaseTask(
	private val databaseDownloadScheduler: DatabaseDownloadScheduler,
) {

	//TODO perhaps return some sort of success or failure thing
	suspend operator fun invoke(dbToDownload: DatabaseAgency): UUID {
		return databaseDownloadScheduler
			.scheduleDatabaseDownloadTask(dbToDownload.name.uppercase())
	}
}