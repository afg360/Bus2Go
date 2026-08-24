package dev.mainhq.bus2go.domain.backgroundtask

import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.NotificationType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface DatabaseDownloadScheduler {

	/** @param databaseAgencyName Must be the name of the dbAgency in all CAPS */
	suspend fun scheduleDatabaseDownloadTask(databaseAgencyName: String): UUID

	/** @param databaseAgencyName Must be the name of the dbAgency in all CAPS */
	fun observeWork(databaseAgencyName: String ): Flow<NotificationType.DbOperation?>
}