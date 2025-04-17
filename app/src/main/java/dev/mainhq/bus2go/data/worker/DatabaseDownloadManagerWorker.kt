package dev.mainhq.bus2go.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.DbToDownload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/** Worker downloading the selected databases at configuration time. */
class DatabaseDownloadManagerWorker(
	context: Context,
	workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

	private lateinit var notificationManager: NotificationManager

	companion object {
		const val KEY = "DB_TO_DOWNLOAD"
	}

	override suspend fun doWork(): Result {
		//check which databases to download
		val dbToDownload = inputData.getString(KEY) ?: throw IllegalStateException("Expected an input")
		val dbDownloadRepository = (applicationContext as Bus2GoApplication).appModule.dbDownloadRepository

		notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		val notifChannel = NotificationChannel("DbDownload", "Database Download", NotificationManager.IMPORTANCE_DEFAULT)
		notificationManager.createNotificationChannel(notifChannel)
		Log.d("DB WORKER", "Started db download work")

		return withContext(Dispatchers.IO){
			val status = when (dbToDownload) {
				"STM" -> dbDownloadRepository.download(DbToDownload.STM)
				"EXO" -> dbDownloadRepository.download(DbToDownload.STM)
				else -> throw IllegalStateException("You forgot to add the correct key")
			}
			if (!status) {
				withContext(Dispatchers.Main) {
					notificationManager.notify(
						1,
						NotificationCompat.Builder(applicationContext, "DbDownload")
							.setSmallIcon(R.drawable.baseline_language_24)
							.setContentTitle("Failed Download")
							.setContentText("Failed to download the $dbToDownload database")
							.build()
					)
				}
				Result.retry()
			}
			else {
				withContext(Dispatchers.Main) {
					Log.d("DB WORKER", "Downloaded succesfully")
					notificationManager.notify(
						1,
						NotificationCompat.Builder(applicationContext, "DbDownload")
							.setSmallIcon(R.drawable.baseline_language_24)
							.setContentTitle("Stm db download")
							.setContentText("Download success")
							.build()
					)
				}
				Result.success()
			}
		}
	}


}