package dev.mainhq.bus2go.data.worker

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.data_source.notifications.NotificationHandler
import dev.mainhq.bus2go.data.repository.DatabaseDownloadRepositoryImpl.Companion.TAG
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.entity.Progress
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates
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

	private lateinit var dbToDownload: DbToDownload

	override suspend fun getForegroundInfo(): ForegroundInfo {
		return ForegroundInfo(
			//FIXME use the repo/domain layer instead
			NotificationHandler.getDbNotificationId(dbToDownload),
			NotificationCompat.Builder(applicationContext, NotificationHandler.dbNotifChannel.id)
				.setContentTitle("Doing some shit")
				.setContentText("CoroutineWorker doing some shit")
				.setSmallIcon(R.drawable.baseline_update)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setOngoing(false)
				.build()
		)
	}

	override suspend fun doWork(): Result {
		val dbToDownloadString =
			inputData.getString(KEY) ?: throw IllegalStateException("Expected an input")

		//notificationsRepository.notify(NotificationType.DbUpdateAvailable(dbToDownload))
		Log.d("DB WORKER", "Started db download work")

		//TODO notify when download starts (and show a bar perhaps)
		// once download done, notify for decompressing
		// finally notify with the priority high that Download Complete
		// and in notifications, show a "tap to restart"
		return withContext(Dispatchers.IO) {
			try {
				dbToDownload = when(dbToDownloadString) {
					"STM" -> {
						DbToDownload.STM
					}
					"EXO" -> {
						DbToDownload.EXO
					}
					else -> {
						throw IllegalStateException("You forgot to add the correct key")
					}
				}
				setForeground(getForegroundInfo())
				when (dbToDownload) {
					DbToDownload.STM -> {
						downloadDb(
							getCurrentDbVersion = appStateRepository::getStmDatabaseVersion,
							updateDbVersion = appStateRepository::updateStmDatabaseVersion,
						)
					}
					DbToDownload.EXO -> {
						downloadDb(
							getCurrentDbVersion = appStateRepository::getExoDatabaseVersion,
							updateDbVersion = appStateRepository::updateExoDatabaseVersion
						)
					}
				}
			}
			catch (e: Exception) {
				Log.e("DB_WORKER", "An exception occurred...\n ${e.message}")
				withContext(Dispatchers.Main){
					notificationsRepository.notify(NotificationType.DbUpdateError(dbToDownload))
				}
				Result.failure()
			}
		}
	}

	private suspend fun downloadDb(
		getCurrentDbVersion: suspend () -> Int,
		updateDbVersion: suspend (Int) -> Unit
	): Result {
		//TODO careful with this part, as a possible race condition may occur if at the same time we
		// are decompressing from already downloaded file
		return when (val res = dbDownloadRepository.getDbUpToDateVersion(dbToDownload)){
			is Bus2GoResult.Error -> throw NetworkException(res.message)
			is Bus2GoResult.Success<Int> -> {
				if (!isAppUpToDate()){
					Log.d("DB_WORKER", "App version not up to date with database")
					withContext(Dispatchers.Main){
						notificationsRepository.notify(NotificationType.DbUpdateError(dbToDownload))
					}
					Result.failure()
				}
				//TODO in another worker, notify when not connected and only compressed file exists
				// (so that they click on "download" to decompress it)
				val currDbVersion = getCurrentDbVersion()

				//the == is important in the case where the file already exists
				//instead of right away decompressing the file, we need to make sure the db doesnt already exist...
				//FIXMe for now, we will do the replacement cause fuck it i want it to work, user does it explicitly
				if (res.data >= currDbVersion) {
					val dbName = when(dbToDownload){
						DbToDownload.STM -> dbDownloadRepository.DB_NAME_STM
						DbToDownload.EXO -> dbDownloadRepository.DB_NAME_EXO
					}

					if (appStateRepository.doesUpToDateCompressedDbExist(dbToDownload, res.data) == null){
						dbDownloadRepository.getDb(dbToDownload, res.data).collect { progress ->
							when(progress) {
								is Progress.Downloading -> {
									withContext(Dispatchers.Main){
										notificationsRepository.notify(
											NotificationType.DbDownloading(
												dbToDownload,
												progress.current,
												progress.contentLength
											)
										)
									}
								}
								Progress.Idle -> {}
								is Progress.Completed -> {
									if (!progress.success){
										withContext(Dispatchers.Main) {
											notificationsRepository.notify(NotificationType.DbUpdateError(dbToDownload))
										}
										Result.retry()
									}
									else {
										Log.d("DB_WORKER", "Downloaded successfully")
									}
								}
								is Progress.Failed -> {
									//TODO some cleanup first
									withContext(Dispatchers.Main) {
										notificationsRepository.notify(NotificationType.DbUpdateError(dbToDownload))
									}
									Result.retry()
								}
							}
						}
					}
					updateDbVersion(res.data)
					dbDownloadRepository.decompressFile(
						//FIXME WRONG ARGS ARE GIVEN
						applicationContext.getDatabasePath("$dbName.db").parentFile?.absolutePath
							?: throw IllegalStateException("Cannot access db directory"),
						dbName,
						res.data
					).collect { progress ->
						when(progress) {
							Progress.Idle -> {
								withContext(Dispatchers.Main) {
									notificationsRepository.notify(NotificationType.DbExtracting(dbToDownload))
								}
							}
							is Progress.Completed -> {
								withContext(Dispatchers.Main) {
									notificationsRepository.notify(NotificationType.DbUpdateDone(dbToDownload))
								}
							}
							else -> throw IllegalStateException("Wtf")
						}
					}
					Result.success(workDataOf("SAME" to false))
				}
				else {
					withContext(Dispatchers.Main){
						Toast.makeText(applicationContext, "Db already exists and is up to date", Toast.LENGTH_SHORT)
							.show()
					}
					Result.success(
						//same as in server side has same data as client
						workDataOf("SAME" to true)
					)
				}
			}
		}
	}

	/**
	 * Checks whether or not the current version code is up to date with the database hosted in
	 * backend server (newer database may be incompatible with older versions of the app).
	 * */
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
					.let { resp.data.min.toLong() <= it && resp.data.max.toLong() >= it }
			}
		}
	}
}