package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.Progress
import kotlinx.coroutines.flow.Flow

interface DatabaseDownloadRepository {
	val DB_NAME_STM: String
	val DB_NAME_EXO: String

	/** Test if the server is a valid bus2go server. */
	suspend fun getIsBus2Go(str: String): Result<Boolean>

	@Throws(IllegalArgumentException::class)
	/** Check what is the most up to date version of the database to download */
	suspend fun getDbUpToDateVersion(dbToDownload: DbToDownload): Result<Int>

	suspend fun getAllDbUpToDateVersion(): Result<Map<DbToDownload, Int>>

	/** Retrieves the minimum app version code needed for the current db versions to be compatible with */
	suspend fun getAppVersionCodeRequired(): Result<AppVersions>

	/**
	 * Download an agency database from a bus2go server if no local up to date compressed file has
	 * been found.
	 * **/
	suspend fun getDb(dbToDownload: DbToDownload, versionNeeded: Int): Flow<Progress>

	/** Decompress the given file if it exists */
	suspend fun decompressFile(dbPath: String, dbName: String, version: Int): Flow<Progress>
}