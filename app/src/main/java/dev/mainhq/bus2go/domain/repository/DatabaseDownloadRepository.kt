package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DbToDownload

interface DatabaseDownloadRepository {

	/** Test if the server is a valid bus2go server. */
	suspend fun testIsBus2Go(): Result<Boolean>

	/** Download to input database from a bus2go server. */
	suspend fun download(dbToDownload: DbToDownload): Boolean
}