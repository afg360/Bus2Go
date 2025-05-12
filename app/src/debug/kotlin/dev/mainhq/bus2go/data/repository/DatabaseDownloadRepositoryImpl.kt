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
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.URLBuilder
import io.ktor.http.set
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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
	private val logger: Logger?
): DatabaseDownloadRepository {

	//FIXME BuildConfig won't exist in alpha version!
	private val baseUrl: String = baseUrl ?: BuildConfig.LOCAL_HOST

	companion object {
		private const val DB_DEBUG_NAME_STM = "stm_info.db"
		private const val DB_DEBUG_NAME_EXO = "exo_info.db"
		private const val COMPRESSION_EXT = "gz"

		private const val TAG = "DATABASE_DOWNLOAD"

		private const val EXPECTED_MESSAGE = "This is a Bus2Go server... potentially"
		private const val API_VERSION = "v1"
	}

	//TODO
	override suspend fun testIsBus2Go(str: String): Result<Boolean> {
		//TODO eventually also set a header to send perhaps to prove identity from client
		if (!networkMonitor.isConnected()) {
			logger?.error(TAG, "Not connected")
			return Result.Error(null, "Not connected to the internet")
		}
		val url = URLBuilder(
			host = str,
			//FIXME to port 443
			port = BuildConfig.DEFAULT_PORT
		).apply {
			set{
				pathSegments = listOf("api", "version")
			}
		}.build()

		try{
			//if we receive an Error, then the url is wrong
			logger?.debug(TAG, url.toString())
			return when(val res = NetworkClient.get(url)){
				is Result.Error -> Result.Success(false)
				is Result.Success<ByteReadChannel> -> {
					//before returning success, read the message and compare
					val response = Json.decodeFromString<JsonObject>(res.data.readRemaining().readText())
					logger?.debug(TAG, response.toString())
					val message = response["message"]?.jsonPrimitive?.content
					val version = response["version"]?.jsonPrimitive?.content
					Result.Success( message == EXPECTED_MESSAGE && version == API_VERSION)
				}
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

	override suspend fun download(dbToDownload: DbToDownload): Boolean {
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


		logger?.debug(TAG, "Beginning Download")
		val downloadStatus = NetworkClient.getAndExecute(url){
			try {
				val channel = it.body<ByteReadChannel>()
				val tmpCompressedFile = File(applicationContext.filesDir, "$dbName.$COMPRESSION_EXT.part")
				//download of compressed databases
				FileOutputStream(tmpCompressedFile).use { outputStream ->
					val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
					while (!channel.isClosedForRead){
						val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
						if (bytesRead <= 0) break
						outputStream.write(buffer, 0, bytesRead)
					}
				}
				true
			}
			catch (ne: NetworkException){
				//TODO SOME LOGGGING
				logger?.error(TAG, "A network exception occured", ne)

					false
					//TODO("Not implemented")
				}
				catch (ioe: IOException){
					logger?.error(TAG, "IOException...", ioe)
					false
				}
		}

		if (downloadStatus){
			logger?.debug(TAG, "Download Successful")
			val compressedFile = File(applicationContext.filesDir, "$dbName.$COMPRESSION_EXT")
			val tmpCompressedFile = File(applicationContext.filesDir, "$dbName.$COMPRESSION_EXT.part")
			if (!tmpCompressedFile.renameTo(compressedFile))
				throw IOException("Failed to rename downloaded file to a compressed file")

			val databasesDir = applicationContext.getDatabasePath(dbName).parentFile
				?: throw IllegalStateException("Cannot access databases directory")

			val destFile = File(databasesDir, dbName)
			if (destFile.exists()) destFile.delete()

			decompressing(compressedFile, destFile)

			logger?.debug(TAG, "Decompressing Successful")
			return true
		}

		return false
	}

	private suspend fun decompressing(compressedFile: File, destFile: File){
		logger?.debug(TAG, "Decompressing")
		withContext(Dispatchers.IO) {
			FileInputStream(compressedFile).use { fileIn ->
				GZIPInputStream(fileIn).use { zstdIn ->
					FileOutputStream(destFile).use { fileOut ->
						val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
						var bytesRead: Int
						while (zstdIn.read(buffer).also { bytesRead = it } != -1) {
							fileOut.write(buffer, 0, bytesRead)
						}
					}
				}
			}
		}
	}

}