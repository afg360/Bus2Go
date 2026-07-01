package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.BuildConfig
import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.Progress
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.DEFAULT_PORT
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.zip.GZIPInputStream

class DebugDatabaseDownloadRepositoryImpl(
	baseUrl: String?,
	override val filesDir: File,
	override val networkMonitor: NetworkMonitor,
	override val logger: Logger?,
): DatabaseDownloadRepositoryAbstractImpl() {

	override val baseHost: String = baseUrl ?: BuildConfig.LOCAL_HOST
	override val tag = "DATABASE_DOWNLOAD"

	override suspend fun getIsBus2Go(str: String): Result<Boolean> {
		return _getIsBus2Go(str, BuildConfig.DEFAULT_PORT)
	}

	override suspend fun getDbUpToDateVersion(dbToDownload: DbToDownload): Result<Int> {
		return _getDbUpToDateVersion(dbToDownload, BuildConfig.DEFAULT_PORT)
	}

	override suspend fun getAllDbUpToDateVersion(): Result<Map<DbToDownload, Int>> {
		return _getAllDbUpToDateVersion(DEFAULT_PORT)
	}

	override suspend fun getAppVersionCodeRequired(): Result<AppVersions> {
		return _getAppVersionCodeRequired(DEFAULT_PORT)
	}

	override fun getDb(dbToDownload: DbToDownload, versionNeeded: Int): Flow<Progress> {
		return _getDb(dbToDownload, versionNeeded, DEFAULT_PORT, filesDir)
	}

	override fun decompressFile(dbPath: String, dbName: String, version: Int): Flow<Progress> {
		return _decompressFile(dbPath, dbName, version)
	}

}