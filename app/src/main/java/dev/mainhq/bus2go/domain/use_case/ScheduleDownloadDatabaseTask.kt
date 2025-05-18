package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.backgroundtask.DatabaseDownloadScheduler
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository

class ScheduleDownloadDatabaseTask(
	private val databaseDownloadScheduler: DatabaseDownloadScheduler,
	private val appStateRepository: AppStateRepository,
	private val dbDownloadRepository: DatabaseDownloadRepository
) {

	//TODO perhaps return some sort of success or failure thing
	suspend operator fun invoke(dbToDownload: DbToDownload){
		val needsDownload = when(dbToDownload){
			DbToDownload.STM -> {
				when (val res = dbDownloadRepository.getDbUpToDateVersion(DbToDownload.STM)){
					is Result.Error -> TODO()
					is Result.Success<Int> -> res.data > appStateRepository.getStmDatabaseVersion()
				}
			}
			DbToDownload.EXO -> {
				when (val res = dbDownloadRepository.getDbUpToDateVersion(DbToDownload.STM)){
					is Result.Error -> TODO()
					is Result.Success<Int> -> res.data > appStateRepository.getExoDatabaseVersion()
				}
			}
			DbToDownload.ALL -> TODO()
		}
		if (needsDownload)
			databaseDownloadScheduler.scheduleDatabaseDownloadTask(dbToDownload)
	}

}