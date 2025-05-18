package dev.mainhq.bus2go.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/** Worker downloading the selected databases at configuration time. */
class DatabaseDownloadManagerWorker(
	context: Context,
	workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

	companion object {
		const val KEY = "DB_TO_DOWNLOAD"
	}

	private val dbDownloadRepository =
		(applicationContext as Bus2GoApplication).appModule.dbDownloadRepository

	private val notificationsRepository =
		(applicationContext as Bus2GoApplication).commonModule.notificationsRepository

	private val appStaterepository =
		(applicationContext as Bus2GoApplication).commonModule.appStateRepository

	override suspend fun doWork(): Result {
		//before downloading whole database, check if database was already fully downloaded
		//to verify if db is up to date, query backend server to check if the file name/version matches
		//check which databases to download
		val dbToDownload =
			inputData.getString(KEY) ?: throw IllegalStateException("Expected an input")

		notificationsRepository.notify(NotificationType.DbUpdateAvailable(dbToDownload))
		Log.d("DB WORKER", "Started db download work")

		//TODO notify when download starts (and show a bar perhaps)
		// once download done, notify for decompressing
		// finally notify with the priority high that Download Complete
		// and in notifications, show a "tap to restart"

		return withContext(Dispatchers.IO) {
			try {
				val status = when (dbToDownload) {
					"STM" -> dbDownloadRepository.getDb(DbToDownload.STM)
					"EXO" -> dbDownloadRepository.getDb(DbToDownload.EXO)
					else -> throw IllegalStateException("You forgot to add the correct key")
				}
				withContext(Dispatchers.Main) {
					if (!status) {
						notificationsRepository.notify(NotificationType.DbUpdateError)
						Result.retry()
					}
					else {
						Log.d("DB_WORKER", "Downloaded successfully")
						notificationsRepository.notify(NotificationType.DbUpdateDone)
						Result.success()
					}
				}
			}
			catch (e: Exception) {
				Log.e("DB_WORKER", "An exception occurred...\n ${e.stackTrace}")
				Result.failure()
			}
		}
	}
}