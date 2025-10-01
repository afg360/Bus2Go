package dev.mainhq.bus2go.data.worker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.PackageManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.mainhq.bus2go.domain.core.Result as Bus2GoResult

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
						downloadDb(
							dbToDownload = DbToDownload.STM,
							getCurrentDbVersion = appStateRepository::getStmDatabaseVersion,
							updateDbVersion = appStateRepository::updateStmDatabaseVersion
						)
					}
					"EXO" -> {
						downloadDb(
							dbToDownload = DbToDownload.EXO,
							getCurrentDbVersion = appStateRepository::getExoDatabaseVersion,
							updateDbVersion = appStateRepository::updateExoDatabaseVersion
						)
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

	private suspend fun downloadDb(
		dbToDownload: DbToDownload,
		getCurrentDbVersion: suspend () -> Int,
		updateDbVersion: suspend (Int) -> Unit
	): Result {
		return when (val res = dbDownloadRepository.getDbUpToDateVersion(dbToDownload)){
			is Bus2GoResult.Error -> throw NetworkException(res.message)
			is Bus2GoResult.Success<Int> -> {
				if (!isAppUpToDate()){
					Log.d("DB_WORKER", "App version not up to date with database")
					withContext(Dispatchers.Main){
						notificationsRepository.notify(NotificationType.DbUpdateError)
					}
					Result.failure()
				}
				if (res.data > getCurrentDbVersion()) {

					if (!dbDownloadRepository.getDb(dbToDownload, res.data)) {
						updateDbVersion(res.data)
						withContext(Dispatchers.Main) {
							notificationsRepository.notify(NotificationType.DbUpdateError)
						}
						Result.retry()
					}
					else {
						Log.d("DB_WORKER", "Downloaded successfully")
						withContext(Dispatchers.Main) {
							notificationsRepository.notify(NotificationType.DbUpdateDone)
						}
						Result.success()
					}
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

	/** Checks whether or not the current version code is up to date with the database hosted in backend server */
	private suspend fun isAppUpToDate(): Boolean {
		//TODO also check if the version is smaller than the max accepted version
		return when(val resp = dbDownloadRepository.getAppVersionCodeRequired()){
			is Bus2GoResult.Error -> false
			is Bus2GoResult.Success<AppVersions> -> {
				applicationContext
					.packageManager
					.getPackageInfo(applicationContext.packageName, 0)
					.let {
						if (Build.VERSION.SDK_INT < 28) {
							it.versionCode.toLong()
						}
						else {
							it.longVersionCode
						}
					}
					.let {
						resp.data.min.toLong() <= it && resp.data.max.toLong() >= it
					}
			}
		}
	}
}