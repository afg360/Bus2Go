package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DbToDownload

interface DatabaseDownloadRepository {

	/** Test if the server is a valid bus2go server. */
	suspend fun getIsBus2Go(str: String): Result<Boolean>

	/** Check what is the most up to date version of the database to download */
	suspend fun getDbUpToDateVersion(dbToDownload: DbToDownload): Result<Int>

	/**
	 * Download an agency database from a bus2go server,
	 * if no local up to date compressed file has been found.
	 **/
	suspend fun getDb(dbToDownload: DbToDownload): Boolean
}