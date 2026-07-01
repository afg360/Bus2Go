package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.Progress
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import io.ktor.client.call.body
import io.ktor.http.URLBuilder
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
import java.util.zip.GZIPInputStream

abstract class DatabaseDownloadRepositoryAbstractImpl: DatabaseDownloadRepository {
	protected abstract val baseHost : String
	protected abstract val filesDir: File
	protected abstract val networkMonitor: NetworkMonitor
	protected abstract val logger: Logger?
	protected abstract val tag: String

	override val DB_NAME_STM = "stm_data"
	override val DB_NAME_EXO = "exo_data"

	companion object {
		const val COMPRESSION_EXT = "gz"
		private const val EXPECTED_MESSAGE = "This is a Bus2Go server... potentially"
		private const val API_VERSION = "v1"
	}

	protected suspend fun _getIsBus2Go(str: String, port: Int): Result<Boolean> {
		//TODO eventually also set a header to send to prove perhaps identity from client
		val url = URLBuilder(
			host = str,
			port = port,
			pathSegments = listOf("api", "version")
		).build()

		return NetworkClient.call(
			url,
			onError = { Result.Success(false) },
			onSuccess = { res ->
				//before returning success, read the message and compare
				val response = Json.decodeFromString<JsonObject>(res.data.readRemaining().readText())
				val message = response["message"]?.jsonPrimitive?.content
				val version = response["version"]?.jsonPrimitive?.content
				message == EXPECTED_MESSAGE && version == API_VERSION
			},
			networkMonitor = networkMonitor,
			logger = logger,
			tag = tag
		)
	}

	protected suspend fun _getDbUpToDateVersion(dbToDownload: DbToDownload, port: Int): Result<Int> {
		val url = URLBuilder(
			host = baseHost,
			port = port,
			pathSegments = listOf("api", "download", API_VERSION, dbToDownload.name.lowercase(), "version")
		).build()
		return NetworkClient.call(
			url,
			onError = { Result.Error(null, "Wrong call to api...?") },
			onSuccess = { res ->
				Json.decodeFromString<JsonObject>(res.data.readRemaining().readText())["version"]
					?.jsonPrimitive?.int ?: -1
			},
			networkMonitor = networkMonitor,
			logger = logger,
			tag = tag
		)
	}

	protected suspend fun _getAllDbUpToDateVersion(port: Int): Result<Map<DbToDownload, Int>> {
		val url = URLBuilder(
			host = baseHost,
			port = port,
			//FIXME needs to be replaced since "all" is not a valid endpoint
			pathSegments = listOf("api", "download", API_VERSION, "versions")
		).build()
		return NetworkClient.call(
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
			},
			networkMonitor = networkMonitor,
			logger = logger,
			tag = tag
		)
	}

	protected suspend fun _getAppVersionCodeRequired(port: Int): Result<AppVersions> {
		return NetworkClient.call(
			url = URLBuilder(
				host = baseHost,
				port = port,
				pathSegments = listOf("api", "download", API_VERSION, "app_version_code_required")
			).build(),
			onError = { Result.Error(null, "Wrong api call") },
			onSuccess = {
				Json.decodeFromString<AppVersions>(it.data.readRemaining().readText())
			},
			networkMonitor = networkMonitor,
			logger = logger,
			tag = tag
		)
	}

	protected fun _getDb(dbToDownload: DbToDownload, versionNeeded: Int, port: Int): Flow<Progress> {
		return flow {
			emit(Progress.Idle)
			val urlBuilder = URLBuilder(
				host = baseHost,
				port = port,
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
					logger?.debug(tag, fileName)
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
		}.flowOn(Dispatchers.IO)
	}

	protected fun _decompressFile(dbPath: String, dbName: String, version: Int): Flow<Progress> {
		return flow {
			//FIXMe for now the final file name doesnt contain db version, add it eventually (see abstrack Room classes)
			// or store it directly inside the db inside a config/metadata table
			val destFile = File(dbPath, "$dbName.db").also { it.parentFile?.mkdirs() }

			val compressedFile = File(filesDir, "${dbName}_${version}.db.gz")
			if (destFile.exists()) destFile.delete()

			logger?.debug(tag, "Decompressing")
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
							logger?.error(tag, ioe.message.toString())
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