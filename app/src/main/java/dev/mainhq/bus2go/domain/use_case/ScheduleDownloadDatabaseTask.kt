package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.repository.DbDownloadRepository

class ScheduleDownloadDatabaseTask(
	private val dbDownloadRepository: DbDownloadRepository
) {

	//TODO perhaps return some sort of success or failure thing
	suspend operator fun invoke(dbToDownload: DbToDownload){
		when(dbToDownload){
			DbToDownload.ALL -> TODO()
			DbToDownload.STM -> dbDownloadRepository.downloadStmDb()
			DbToDownload.EXO -> TODO()
		}
	}

}