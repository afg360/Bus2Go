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
import dev.mainhq.bus2go.domain.entity.NotificationType
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.set
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
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

class DatabaseDownloadRepositoryImpl(
	baseUrl: String?,
	private val networkMonitor: NetworkMonitor,
	private val filesDir: File,
	private val logger: Logger?
): DatabaseDownloadRepository {

	private var baseUrl: String = baseUrl ?: BuildConfig.LOCAL_HOST

	//TODO should be used in case we change the url
	fun setBaseUrl(url: String){
		baseUrl = url
	}

	override val DB_NAME_STM = "stm_data"
	override val DB_NAME_EXO = "exo_data"

	companion object {
		const val COMPRESSION_EXT = "gz"

		const val TAG = "DATABASE_DOWNLOAD"

		private const val EXPECTED_MESSAGE = "This is a Bus2Go server... potentially"
		private const val API_VERSION = "v1"
	}

	override suspend fun getIsBus2Go(str: String): Result<Boolean> {
		//TODO eventually also set a header to send to prove perhaps identity from client
		val url = URLBuilder(
			host = str,
			//FIXME to port 443
			port = BuildConfig.DEFAULT_PORT,
			pathSegments = listOf("api", "version")
		).build()

		return call(
			url,
			onError = { Result.Success(false) },
			onSuccess = { res ->
				//before returning success, read the message and compare
				val response = Json.decodeFromString<JsonObject>(res.data.readRemaining().readText())
				logger?.debug(TAG, response.toString())
				val message = response["message"]?.jsonPrimitive?.content
				val version = response["version"]?.jsonPrimitive?.content
				message == EXPECTED_MESSAGE && version == API_VERSION
			}
		)
	}

	override suspend fun getDbUpToDateVersion(dbToDownload: DbToDownload): Result<Int> {
		val url = URLBuilder(
			host = baseUrl,
			port = BuildConfig.DEFAULT_PORT,
			pathSegments = listOf("api", "download", API_VERSION, dbToDownload.name.lowercase(), "version")
		).build()
		return call(
			url,
			onError = { Result.Error(null, "Wrong call to api...?") },
			onSuccess = { res ->
				Json.decodeFromString<JsonObject>(res.data.readRemaining().readText())["version"]
					?.jsonPrimitive?.int ?: -1
			}
		)
	}

	override suspend fun getAllDbUpToDateVersion(): Result<Map<DbToDownload, Int>> {
		val url = URLBuilder(
			host = baseUrl,
			port = BuildConfig.DEFAULT_PORT,
			//FIXME needs to be replaced since "all" is not a valid endpoint
			pathSegments = listOf("api", "download", API_VERSION, "versions")
		).build()
		return call(
			url,
			onError = { Result.Error(null, "Wrong call to api...?") },
			onSuccess = { res ->
				Json.decodeFromString<JsonArray>(res.data.readRemaining().readText())
					.flatMap {
						when(it.jsonObject["database"]?.jsonPrimitive?.toString()) {
							"stm" -> {
								listOf(DbToDownload.STM to (it.jsonObject["version"]?.jsonPrimitive?.int ?: -1))
							}
							"exo" -> {
								listOf(DbToDownload.EXO to (it.jsonObject["version"]?.jsonPrimitive?.int ?: -1))
							}
							else -> { listOf() }
						}
					}
					.toMap()
			}
		)
	}

	override suspend fun getAppVersionCodeRequired(): Result<AppVersions> {
		return call(
			url = URLBuilder()
				.apply {
					host = BuildConfig.LOCAL_HOST
					port = BuildConfig.DEFAULT_PORT
					pathSegments = listOf("api", "download", API_VERSION, "app_version_code_required")
				}
				.build(),
			onError = { Result.Error(null, "Wrong api call") },
			onSuccess = {
				Json.decodeFromString<AppVersions>(it.data.readRemaining().readText())
			}
		)
	}


	/** A helper function dealing with formatting correctly the network call and doing basic checks */
	private suspend fun <T> call(
		url: Url,
		onError: () -> Result<T>,
		onSuccess: suspend (Result.Success<ByteReadChannel>) -> T
	): Result<T>{
		if (!networkMonitor.isConnected()) {
			logger?.error(TAG, "Not connected")
			return Result.Error(null, "Not connected to the internet")
		}

		try{
			//if we receive an Error, then the url is wrong
			logger?.debug(TAG, url.toString())
			return when(val res = NetworkClient.get(url)){
				is Result.Error -> onError()
				is Result.Success<ByteReadChannel> -> Result.Success(onSuccess(res))
			}
		}
		catch (iae: IllegalArgumentException){
			logger?.error(TAG, "Malformed URL", iae)
			return Result.Error(null, "The URL was malformed")
		}
		catch (coe: ConnectTimeoutException){
			logger?.error(TAG, "Connection timed out...", coe)
			return Result.Error(null, "Connection has timed out")
		}
		catch (uho: UnknownHostException){
			logger?.error(TAG, "Unknown host", uho)
			return Result.Error(null, "The host does not exist")
		}
		catch (ce: ConnectException){
			logger?.error(TAG, "Connection Exception", ce)
			return Result.Error(null, "Cannot connect to the server")
		}
		catch (ioe: IOException){
			logger?.error(TAG, "Unknown IOException occurred", ioe)
			return Result.Error(ioe, null)
		}
	}

	override suspend fun getDb(dbToDownload: DbToDownload, versionNeeded: Int): Flow<Progress> {
		return flow {
			emit(Progress.Idle)

			val urlBuilder = URLBuilder(
				host = baseUrl,
				port = BuildConfig.DEFAULT_PORT,
				pathSegments = when(dbToDownload){
					DbToDownload.STM -> listOf("api", "download", API_VERSION, "stm")//"debug", "sample_data", "stm")
					DbToDownload.EXO -> listOf("api", "download", API_VERSION, "exo")
				}
			)
			val url = urlBuilder.build()

			//saves the file in the filesDir, needs to be moved to the databases dir
			val dbNameList = when(dbToDownload){
				DbToDownload.STM -> listOf(DB_NAME_STM)
				DbToDownload.EXO -> listOf(DB_NAME_EXO)
			}

			NetworkClient.getAndExecute(url){
				var tmpCompressedFile: File? = null
				try {
					val contentLength = it.headers["content-length"]?.toInt()
						?: throw NetworkException("Content-Length HTTP header not set by server...")
					val channel = it.body<ByteReadChannel>()
					//FIXME better parsing should happen here in case quotes and other garbage is added...
					val fileName = it.headers["content-disposition"]?.substringAfter("attachment; filename=")
						?: throw NetworkException("Content-Disposition HTTP header needed for file name not set by server...")
					logger?.debug(TAG, fileName)
					tmpCompressedFile = File(filesDir, "$fileName.part")
					var lastNotifTime = System.currentTimeMillis()
					var totalDownloaded = 0

					emit(Progress.Downloading(0, contentLength))
					//download of compressed databases
					FileOutputStream(tmpCompressedFile).use { outputStream ->
						val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
						while (!channel.isClosedForRead){
							val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
							if (bytesRead <= 0) break
							outputStream.write(buffer, 0, bytesRead)
							totalDownloaded += bytesRead

							//show a new notification only every 1.5 secs...
							val currentTime = System.currentTimeMillis()
							if ((currentTime - lastNotifTime) > 1500L) {
								lastNotifTime = currentTime
								emit(Progress.Downloading(totalDownloaded, contentLength))
							}
						}
					}

					val compressedFile = File(filesDir, "${dbNameList.first()}_${versionNeeded}.db.gz")
					//FIXME how to find name if it is variable...
					if (!tmpCompressedFile.renameTo(compressedFile))
						throw IOException("Failed to rename downloaded file to a compressed file")
					emit(Progress.Completed(totalDownloaded >= contentLength))

					//also, should probably simply perform a hashing thing to be sure it has been done correctly
				}
				catch (ne: NetworkException){
					emit(Progress.Failed("A network exception occured", ne))
					tmpCompressedFile?.delete()
				}
				catch (ioe: IOException){
					emit(Progress.Failed("IOException...", ioe))
					tmpCompressedFile?.delete()
				}
				catch (e: Exception){
					emit(Progress.Failed("Exception...", e))
					tmpCompressedFile?.delete()
				}
			}
		}
	}

	override suspend fun decompressFile(dbPath: String, dbName: String, version: Int): Flow<Progress> {
		return flow {
			//FIXMe for now the final file name doesnt contain db version, add it eventually (see abstrack Room classes)
			// or store it directly inside the db inside a config/metadata table
			val destFile = File(dbPath, "$dbName.db").also { it.parentFile?.mkdirs() }

			val compressedFile = File(filesDir, "${dbName}_${version}.db.gz")
			if (destFile.exists()) destFile.delete()

			logger?.debug(TAG, "Decompressing")
			FileInputStream(compressedFile).use { fileIn ->
				GZIPInputStream(fileIn).use { zstdIn ->
					FileOutputStream(destFile).use { fileOut ->
						emit(Progress.Idle)
						val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
						var bytesRead: Int
						try{
							while (zstdIn.read(buffer).also { bytesRead = it } != -1) {
								fileOut.write(buffer, 0, bytesRead)
							}
							emit(Progress.Completed(true))
						}
						catch (ioe: IOException){
							logger?.error(TAG, ioe.message.toString())
							//delete garbage/corrupted files
							if (compressedFile.exists()) compressedFile.delete()
							if (destFile.exists()) destFile.delete()
							emit(Progress.Completed(false))
						}
					}
				}
			}
		}.flowOn(Dispatchers.IO)
	}

}