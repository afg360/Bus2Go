package dev.mainhq.bus2go.data.worker

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//FIXME needs refactoring to domain layer...
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

	private val appStateRepository =
		(applicationContext as Bus2GoApplication).commonModule.appStateRepository

	override suspend fun doWork(): Result {
		val dbToDownload =
			inputData.getString(KEY) ?: throw IllegalStateException("Expected an input")

		//notificationsRepository.notify(NotificationType.DbUpdateAvailable(dbToDownload))
		Log.d("DB WORKER", "Started db download work")

		//TODO notify when download starts (and show a bar perhaps)
		// once download done, notify for decompressing
		// finally notify with the priority high that Download Complete
		// and in notifications, show a "tap to restart"

		return withContext(Dispatchers.IO) {
			try {
				when (dbToDownload) {
					"STM" -> {
						when (val res = dbDownloadRepository.getDbUpToDateVersion(DbToDownload.STM)){
							is dev.mainhq.bus2go.domain.core.Result.Error -> throw NetworkException(res.message)
							is dev.mainhq.bus2go.domain.core.Result.Success<Int> -> {
								if (res.data > appStateRepository.getStmDatabaseVersion())
									helper {
										if (dbDownloadRepository.getDb(DbToDownload.STM, res.data)){
											appStateRepository.updateStmDatabaseVersion(res.data)
											true
										}
										else false
									}
								else {
									withContext(Dispatchers.Main){
										Toast.makeText(applicationContext, "Db already exists", Toast.LENGTH_SHORT)
											.show()
									}
									Result.success()
								}
							}
						}
					}
					"EXO" -> {
						when (val res = dbDownloadRepository.getDbUpToDateVersion(DbToDownload.EXO)){
							is dev.mainhq.bus2go.domain.core.Result.Error -> throw NetworkException(res.message)
							is dev.mainhq.bus2go.domain.core.Result.Success<Int> -> {
								if (res.data > appStateRepository.getExoDatabaseVersion())
									helper{
										if (dbDownloadRepository.getDb(DbToDownload.EXO, res.data)){
											appStateRepository.updateExoDatabaseVersion(res.data)
											true
										}
										else false
									}
								else {
									withContext(Dispatchers.Main){
										Toast.makeText(applicationContext, "Db already exists", Toast.LENGTH_SHORT)
											.show()
									}
									Result.success()
								}
							}
						}
					}
					else -> throw IllegalStateException("You forgot to add the correct key")
				}
			}
			catch (e: Exception) {
				Log.e("DB_WORKER", "An exception occurred...\n ${e.stackTrace}")
				withContext(Dispatchers.Main){
					notificationsRepository.notify(NotificationType.DbUpdateError)
				}
				Result.failure()
			}
		}
	}

	private suspend fun helper(block: suspend () -> Boolean): Result{
		return if (!block()) {
			withContext(Dispatchers.Main){
				notificationsRepository.notify(NotificationType.DbUpdateError)
			}
			Result.retry()
		}
		else {
			Log.d("DB_WORKER", "Downloaded successfully")
			withContext(Dispatchers.Main){
				notificationsRepository.notify(NotificationType.DbUpdateDone)
			}
			Result.success()
		}
	}
}