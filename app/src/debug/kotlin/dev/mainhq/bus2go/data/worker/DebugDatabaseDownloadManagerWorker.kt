package dev.mainhq.bus2go.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.BuildConfig
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import io.ktor.client.call.body
import io.ktor.http.URLBuilder
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.bits.Memory
import io.ktor.utils.io.bits.of
import io.ktor.utils.io.core.Buffer
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.use
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class DebugDatabaseDownloadManagerWorker(
	context: Context,
	workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

	private lateinit var baseUrl: String

	companion object {
		private const val DB_DEBUG_NAME = "stm_info.db"
	}

	private lateinit var notificationManager: NotificationManager

	override suspend fun doWork(): Result {
		//check which databases to download
		val isStm = inputData.getBoolean("stm", false)
		val isExo = inputData.getBoolean("exo", false)
		baseUrl = inputData.getString("baseUrl") ?: BuildConfig.LOCAL_HOST
		val jobs: MutableList<Deferred<Boolean>> = mutableListOf()
		notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		val notifChannel = NotificationChannel("DbDownload", "Database Download", NotificationManager.IMPORTANCE_DEFAULT)

		notificationManager.createNotificationChannel(notifChannel)
		Log.d("DB WORKER", "Started db download work")
		//FIXME downloading initiates but does not seem to save files properly...?
		if (withContext(Dispatchers.IO){
				if (isStm) jobs.add(async{return@async downloadStmDb()})
				//if (isExo) downloadAllExo(this, jobs)

				return@withContext jobs.map { it.await() }.reduce{b1, b2 -> b1 && b2}
			}){
			Log.d("DB WORKER", "Downloaded succesfully")
			notificationManager.notify(
				1,
				NotificationCompat.Builder(applicationContext, "DbDownload")
					.setSmallIcon(R.drawable.baseline_language_24)
					.setContentTitle("Stm db download")
					.setContentText("Download success")
					.build()
			)
			return Result.success()
		}
		else return Result.failure()
	}

	private suspend fun downloadStmDb(): Boolean{
		val url = URLBuilder(
			host = baseUrl,
			port = BuildConfig.DEFAULT_PORT,
			pathSegments = listOf("api", "debug", "sample_data", "stm")
		).build()
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
						yield()
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