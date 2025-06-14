package dev.mainhq.bus2go.data.repository

import android.content.Context
import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.set
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.zip.GZIPInputStream

import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository
import dev.mainhq.bus2go.domain.repository.NotificationsRepository
import dev.mainhq.bus2go.domain.repository.SettingsRepository

class DatabaseDownloadRepositoryImpl(
	private val applicationContext: Context,
	private val networkMonitor: NetworkMonitor,
	private val notificationsRepository: NotificationsRepository,
	private val settingsRepository: SettingsRepository
): DatabaseDownloadRepository {

	companion object {
		private const val DEFAULT_PORT = 443//8000
		private const val DB_DEBUG_NAME_STM = "stm_data"
		private const val DB_DEBUG_NAME_EXO = "exo_data"
		private const val COMPRESSION_EXT = "gz"

		private const val EXPECTED_MESSAGE = "This is a Bus2Go server... potentially"
		private const val API_VERSION = "v1"
	}

	//we must make this call every time instead of holding a reference to the server choice because
	// it may change overtime
	private val host get() = settingsRepository.getSettings().serverChoice


	override suspend fun getIsBus2Go(str: String): Result<Boolean> {
		//TODO eventually also set a header to send to prove perhaps identity from client
		val url = URLBuilder(
			host = str,
			port = DEFAULT_PORT
		).apply {
			set{
				pathSegments = listOf("api", "version")
			}
		}.build()

		return call(
			url,
			onError = { Result.Success(false) },
			onSuccess = { res ->
				//before returning success, read the message and compare
				val response = Json.decodeFromString<JsonObject>(res.data.readRemaining().readText())
				val message = response["message"]?.jsonPrimitive?.content
				val version = response["version"]?.jsonPrimitive?.content
				message == EXPECTED_MESSAGE && version == API_VERSION
			}
		)
	}

	override suspend fun getDbUpToDateVersion(dbToDownload: DbToDownload): Result<Int> {
		val url = URLBuilder(
			host = host,
			port = DEFAULT_PORT,
		).apply {
			set {
				//FIXME needs to be replaced since "all" is not a valid endpoint
				pathSegments = listOf("api", "download", "v1", dbToDownload.name.lowercase(), "version")
			}
		}.build()
		return call(
			url,
			onError = { Result.Error(null, "Wrong call to api...?") },
			onSuccess = { res ->
				Json.decodeFromString<JsonObject>(res.data.readRemaining().readText())["version"]
					?.jsonPrimitive?.int ?: -1
			}
		)
	}

	private suspend fun <T> call(
		url: Url,
		onError: () -> Result<T>,
		onSuccess: suspend (Result.Success<ByteReadChannel>) -> T
	): Result<T>{
		if (!networkMonitor.isConnected()) {
			return Result.Error(null, "Not connected to the internet")
		}

		try{
			//if we receive an Error, then the url is wrong
			return when(val res = NetworkClient.get(url)){
				is Result.Error -> onError()
				is Result.Success<ByteReadChannel> -> Result.Success(onSuccess(res))
			}
		}
		catch (iae: IllegalArgumentException){
			return Result.Error(null, "The URL was malformed")
		}
		catch (coe: ConnectTimeoutException){
			return Result.Error(null, "Connection has timed out")
		}
		catch (uho: UnknownHostException){
			return Result.Error(null, "The host does not exist")
		}
		catch (ce: ConnectException){
			return Result.Error(null, "Cannot connect to the server")
		}
		catch (ioe: IOException){
			return Result.Error(ioe, null)
		}
	}

	override suspend fun getDb(dbToDownload: DbToDownload, versionNeeded: Int): Boolean {
		val urlBuilder = URLBuilder(
			host = host,
			port = DEFAULT_PORT,
		)
		urlBuilder.set {
			pathSegments = when(dbToDownload){
				DbToDownload.ALL -> listOf("api", "debug", "sample_data", "stm")
				DbToDownload.STM -> listOf("api", "download", "v1", "stm")
				DbToDownload.EXO -> listOf("api", "download", "v1", "exo")
			}
		}
		val url = urlBuilder.build()

		//TODO make async calls
		if (dbToDownload == DbToDownload.ALL){
			urlBuilder.set { pathSegments = listOf("api", "download", "v1", "exo") }
			val newUrl = urlBuilder.build()
		}

		//saves the file in the filesDir, needs to be moved to the databases dir
		val dbName = when(dbToDownload){
			DbToDownload.STM -> DB_DEBUG_NAME_STM
			DbToDownload.EXO -> DB_DEBUG_NAME_EXO
			DbToDownload.ALL -> TODO()
		}

		//TODO before downloading, check if file exists already with the correct version
		val mostUpToDateDownloadFile = applicationContext.filesDir.listFiles()
			?.find { it.name.matches("${dbName}_${versionNeeded}.db.${COMPRESSION_EXT}".toRegex())}
		//clean up old versions
		applicationContext.filesDir.listFiles()
			?.filter {
				it.name != mostUpToDateDownloadFile?.name &&
						it.name.matches("${dbName}_[0-9]+.db.${COMPRESSION_EXT}".toRegex())
			}?.forEach { if (it.exists()) it.delete() }
		if (mostUpToDateDownloadFile != null) {
			decompressing(
				mostUpToDateDownloadFile,
				"$dbName.db"
			)
			return true
		}

		var fileName: String? = null
		val downloadStatus = NetworkClient.getAndExecute(url){
			var tmpCompressedFile: File? = null
			try {
				val contentLength = it.headers["content-length"]?.toInt()
					?: throw NetworkException("Content-Length HTTP header not set by server...")
				val channel = it.body<ByteReadChannel>()
				//FIXME better parsing should happen here in case quotes and other garbage is added...
				fileName = it.headers["content-disposition"]?.substringAfter("attachment; filename=")
					?: throw NetworkException("Content-Disposition HTTP header needed for file name not set by server...")
				tmpCompressedFile = File(applicationContext.filesDir, "$fileName.part")
				var totalDownloaded = 0
				notificationsRepository.notify(NotificationType.DbDownloading(0, contentLength))
				//download of compressed databases
				FileOutputStream(tmpCompressedFile).use { outputStream ->
					val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
					while (!channel.isClosedForRead){
						val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
						if (bytesRead <= 0) break
						outputStream.write(buffer, 0, bytesRead)
						totalDownloaded += bytesRead
						notificationsRepository.notify(
							NotificationType.DbDownloading(totalDownloaded, contentLength)
						)
					}
				}
				true
			}
			catch (ne: NetworkException){
				tmpCompressedFile?.delete()
				false
			}
			catch (ioe: IOException){
				tmpCompressedFile?.delete()
				false
			}
			catch (e: Exception){
				tmpCompressedFile?.delete()
				false
			}
		}

		if (downloadStatus){
			//FIXME how to find name if it is variable...
			val compressedFile = File(applicationContext.filesDir, fileName!!)
			val tmpCompressedFile = File(applicationContext.filesDir, "${fileName!!}.part")
			if (!tmpCompressedFile.renameTo(compressedFile))
				throw IOException("Failed to rename downloaded file to a compressed file")

			val decompressingStatus = decompressing(compressedFile, "$dbName.db")
			return decompressingStatus
		}

		return false
	}

	private suspend fun decompressing(compressedFile: File, dbName: String): Boolean {
		val databasesDir = applicationContext.getDatabasePath(dbName).parentFile
			?: throw IllegalStateException("Cannot access databases directory")

		val destFile = File(databasesDir, dbName)
		if (destFile.exists()) destFile.delete()

		return withContext(Dispatchers.IO) {
			FileInputStream(compressedFile).use { fileIn ->
				GZIPInputStream(fileIn).use { zstdIn ->
					FileOutputStream(destFile).use { fileOut ->
						notificationsRepository.notify(NotificationType.DbExtracting)
						val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
						var bytesRead: Int
						try{
							while (zstdIn.read(buffer).also { bytesRead = it } != -1) {
								fileOut.write(buffer, 0, bytesRead)
							}
							true
						}
						catch (ioe: IOException){
							//delete garbage/corrupted files
							if (compressedFile.exists()) compressedFile.delete()
							if (destFile.exists()) destFile.delete()
							false
						}
					}
				}
			}
		}
	}
}