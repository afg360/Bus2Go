package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.Progress
import dev.mainhq.bus2go.domain.entity.ServerChoice
import kotlinx.coroutines.flow.Flow

interface DatabaseDownloadRepository {
	val DB_NAME_STM: String
	val DB_NAME_EXO: String

	/**
	 * Tests if the server is a valid bus2go server. To be used ONLY when using a web server from
	 * official domain names.
	 * @param str Not a necessarily a valid Bus2Go url
	 * */
	suspend fun getIsBus2Go(str: String, isSelfHosted: Boolean): Result<Boolean>

	/** Check what is the most up to date version of the database to download */
	suspend fun getDbUpToDateVersion(serverChoice: ServerChoice, dbToDownload: DbToDownload): Result<Int>

	suspend fun getAllDbUpToDateVersion(serverChoice: ServerChoice): Result<Map<DbToDownload, Int>>

	/** Retrieves the minimum app version code needed for the current db versions to be compatible with */
	suspend fun getAppVersionCodeRequired(serverChoice: ServerChoice): Result<AppVersions>

	/**
	 * Download an agency database from a bus2go server if no local up to date compressed file has
	 * been found.
	 * **/
	fun getDb(serverChoice: ServerChoice, dbToDownload: DbToDownload, versionNeeded: Int): Flow<Progress>

	/** Decompress the given file if it exists */
	fun decompressFile(dbPath: String, dbName: String, version: Int): Flow<Progress>
}