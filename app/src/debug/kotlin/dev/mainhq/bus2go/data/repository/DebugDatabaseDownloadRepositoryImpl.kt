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
	defaultPort: Int?,
	override val filesDir: File,
	override val networkMonitor: NetworkMonitor,
	override val logger: Logger?,
): DatabaseDownloadRepositoryAbstractImpl() {

	override val baseHost: String = baseUrl ?: BuildConfig.LOCAL_HOST
	override val defaultPort = defaultPort ?: BuildConfig.DEFAULT_PORT
	override val tag = "DATABASE_DOWNLOAD"

	override suspend fun getIsBus2Go(str: String, isSelfHosted: Boolean): Result<Boolean> {
		return super._getIsBus2Go(str, false)
	}

	override suspend fun getDbUpToDateVersion(dbToDownload: DbToDownload, isSelfHosted: Boolean): Result<Int> {
		return super._getDbUpToDateVersion(dbToDownload, false)
	}

	override suspend fun getAllDbUpToDateVersion(isSelfHosted: Boolean): Result<Map<DbToDownload, Int>> {
		return super._getAllDbUpToDateVersion(false)
	}

	override suspend fun getAppVersionCodeRequired(isSelfHosted: Boolean): Result<AppVersions> {
		return super._getAppVersionCodeRequired(false)
	}

	override fun getDb(dbToDownload: DbToDownload, versionNeeded: Int, isSelfHosted: Boolean): Flow<Progress> {
		return super._getDb(dbToDownload, versionNeeded, isSelfHosted)
	}
}