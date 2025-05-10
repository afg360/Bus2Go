package dev.mainhq.bus2go.data.repository

import android.content.Context
import dev.mainhq.bus2go.BuildConfig
import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository
import dev.mainhq.bus2go.domain.core.Result
import io.ktor.client.call.body
import io.ktor.http.URLBuilder
import io.ktor.http.set
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream

class DatabaseDownloadRepositoryImpl(
	baseUrl: String?,
	private val applicationContext: Context,
	private val logger: Logger?
): DatabaseDownloadRepository {

	private val baseUrl: String = baseUrl ?: BuildConfig.LOCAL_HOST

	companion object {
		private const val DB_DEBUG_NAME_STM = "stm_info.db"
		private const val DB_DEBUG_NAME_EXO = "exo_info.db"
		private const val COMPRESSION_EXT = "gz"

		private const val TAG = "DATABASE_DOWNLOAD"
	}

	//TODO
	override suspend fun testIsBus2Go(): Result<Boolean> {
		return Result.Success(false)
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