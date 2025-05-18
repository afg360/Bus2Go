package dev.mainhq.bus2go.data.repository

import android.content.Context
import dev.mainhq.bus2go.BuildConfig
import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.repository.NotificationsRepository
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

class DatabaseDownloadRepositoryImpl(
	//TODO...
	baseUrl: String?,
	private val applicationContext: Context,
	private val networkMonitor: NetworkMonitor,
	private val notificationsRepository: NotificationsRepository,
	private val logger: Logger?
): DatabaseDownloadRepository {

	//FIXME BuildConfig won't exist in alpha version!
	private val baseUrl: String = baseUrl ?: BuildConfig.LOCAL_HOST

	companion object {
		private const val DB_DEBUG_NAME_STM = "stm_data"
		private const val DB_DEBUG_NAME_EXO = "exo_data"
		private const val COMPRESSION_EXT = "gz"

		private const val TAG = "DATABASE_DOWNLOAD"

		private const val EXPECTED_MESSAGE = "This is a Bus2Go server... potentially"
		private const val API_VERSION = "v1"
	}

	//TODO
	override suspend fun getIsBus2Go(str: String): Result<Boolean> {
		//TODO eventually also set a header to send to prove perhaps identity from client
		val url = URLBuilder(
			host = str,
			//FIXME to port 443
			port = BuildConfig.DEFAULT_PORT
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
			logger?.error(TAG, "Unknown IOException occured", ioe)
			return Result.Error(ioe, null)
		}
	}

	override suspend fun getDb(dbToDownload: DbToDownload): Boolean {
		val urlBuilder = URLBuilder(
			host = baseUrl,
			port = BuildConfig.DEFAULT_PORT,
		)
		urlBuilder.set {
			pathSegments = when(dbToDownload){
				DbToDownload.ALL -> listOf("api", "debug", "sample_data", "stm")
				DbToDownload.STM -> listOf("api", "debug", "sample_data", "stm")
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
		logger?.debug(TAG, "Looking for already downloaded databases")
		val mostUpToDateDownloadFileList = applicationContext.filesDir.listFiles()
			?.filter { it.name.matches("${dbName}_[0-9]+.db.${COMPRESSION_EXT}".toRegex()) }
		if (mostUpToDateDownloadFileList?.isNotEmpty() == true) {
			val mostUpToDateDownloadFile = mostUpToDateDownloadFileList.reduce{ f1, f2 ->
				val versionStr1 = f1.name.filter { it.isDigit() }.toInt()
				val versionStr2 = f2.name.filter { it.isDigit() }.toInt()
				if (versionStr1 > versionStr2) f1 else f2
			}
			//TODO if there are more than 1, delete the ones that have different version numbers
			if (mostUpToDateDownloadFile != null) {
				decompressing(
					mostUpToDateDownloadFile,
					"$dbName.db"
				)
				return true
			}
		}

		logger?.debug(TAG, "No Db found. Beginning Download")
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
				logger?.debug(TAG, fileName!!)
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
				logger?.error(TAG, "A network exception occured", ne)
				tmpCompressedFile?.delete()
				false
			}
			catch (ioe: IOException){
				logger?.error(TAG, "IOException...", ioe)
				tmpCompressedFile?.delete()
				false
			}
			catch (e: Exception){
				logger?.error(TAG, "Exception...", e)
				tmpCompressedFile?.delete()
				false
			}
		}

		if (downloadStatus){
			logger?.debug(TAG, "Download Successful")
			//FIXME how to find name if it is variable...
			val compressedFile = File(applicationContext.filesDir, fileName!!)
			val tmpCompressedFile = File(applicationContext.filesDir, "${fileName!!}.part")
			if (!tmpCompressedFile.renameTo(compressedFile))
				throw IOException("Failed to rename downloaded file to a compressed file")

			val decompressingStatus = decompressing(compressedFile, "$dbName.db")
			if (decompressingStatus) logger?.debug(TAG, "Decompressing Successful")
			return decompressingStatus
		}

		return false
	}

	private suspend fun decompressing(compressedFile: File, dbName: String): Boolean {
		val databasesDir = applicationContext.getDatabasePath(dbName).parentFile
			?: throw IllegalStateException("Cannot access databases directory")

		val destFile = File(databasesDir, dbName)
		if (destFile.exists()) destFile.delete()

		logger?.debug(TAG, "Decompressing")
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
							return@withContext true
						}
						catch (ioe: IOException){
							logger?.error(TAG, ioe.message.toString())
							//delete garbage/corrupted files
							if (compressedFile.exists()) compressedFile.delete()
							if (destFile.exists()) destFile.delete()
							return@withContext false
						}
					}
				}
			}
		}
	}

}