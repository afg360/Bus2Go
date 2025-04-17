package dev.mainhq.bus2go.data.repository

import android.content.Context
import android.util.Log
import dev.mainhq.bus2go.BuildConfig
import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.repository.DatabaseDownloadRepository
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import io.ktor.client.call.body
import io.ktor.http.URLBuilder
import io.ktor.http.set
import io.ktor.utils.io.ByteReadChannel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class DatabaseDownloadRepositoryImpl(
	baseUrl: String?,
	private val applicationContext: Context
): DatabaseDownloadRepository {

	private val baseUrl: String = baseUrl ?: BuildConfig.LOCAL_HOST

	companion object {
		private const val DB_DEBUG_NAME = "stm_info.db"
	}

	override suspend fun download(dbToDownload: DbToDownload): Boolean {
		val urlBuilder = URLBuilder(
			host = baseUrl,
			port = BuildConfig.DEFAULT_PORT,
		)
		urlBuilder.set {
			pathSegments = when(dbToDownload){
				DbToDownload.ALL -> listOf("api", "debug", "sample_data")
				DbToDownload.STM -> listOf("api", "debug", "sample_data", "stm")
				DbToDownload.EXO -> listOf("api", "debug", "sample_data", "exo")
			}
		}
		val url = urlBuilder.build()
		return NetworkClient.getAndExecute(url){
				try {
					val channel = it.body<ByteReadChannel>()
					//saves the file in the filesDir, needs to be moved to the databases dir
					val tmpFile = File(applicationContext.filesDir, "$DB_DEBUG_NAME.part")
					FileOutputStream(tmpFile).use { outputStream ->
						val buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
						while (!channel.isClosedForRead){
							val bytesRead = channel.readAvailable(buffer)
							if (bytesRead <= 0) break
							outputStream.write(buffer.array(), 0, bytesRead)
						}
					}
					Log.d("DATABASE", "Moving to databases directory")
					val databasesDir = applicationContext.getDatabasePath(DB_DEBUG_NAME).parentFile
						?: throw IllegalStateException("Cannot access databases directory")
					tmpFile.renameTo(File(databasesDir, DB_DEBUG_NAME))
					true
				}
				catch (ne: NetworkException){
					//TODO SOME LOGGGING
					Log.e("DB_DOWNLOAD", "A network exception occured: " + ne.message)
					false
					//TODO("Not implemented")
				}
				catch (ioe: IOException){
					Log.e("DB_DOWNLOAD", "IOException...")
					Log.e("VAL", ioe.message ?: "")
					false
				}
		}
	}

}