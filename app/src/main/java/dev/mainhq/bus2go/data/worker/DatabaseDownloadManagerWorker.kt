package dev.mainhq.bus2go.data.worker

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.data_source.notifications.NotificationHandler
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.entity.Progress
import dev.mainhq.bus2go.domain.exceptions.NetworkException
import dev.mainhq.bus2go.utils.findCause
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
		const val WORK_PROGRESS_CURRENT = "PROGRESS_CURRENT"
		const val WORK_PROGRESS_MAX = "PROGRESS_MAX"
		const val WORK_PROGRESS_IS_DECOMPRESSING = "PROGRESS_IS_DECOMPRESSING"
	}

	private val dbDownloadRepository =
		(applicationContext as Bus2GoApplication).appModule.dbDownloadRepository

	private val notificationsRepository =
		(applicationContext as Bus2GoApplication).commonModule.notificationsRepository

	private val appStateRepository =
		(applicationContext as Bus2GoApplication).commonModule.appStateRepository

	private val settingsRepository =
		(applicationContext as Bus2GoApplication).commonModule.settingsRepository

	private lateinit var databaseAgency: DatabaseAgency

	private val dbToDownloadString = inputData.getString(KEY) ?: throw IllegalStateException("Expected an input")

	override suspend fun getForegroundInfo(): ForegroundInfo {
		setProgress(workDataOf(KEY to databaseAgency.name.uppercase()))
		return ForegroundInfo(
			//FIXME use the repo/domain layer instead
			NotificationHandler.getDbNotificationId(databaseAgency),
			NotificationCompat.Builder(applicationContext, NotificationHandler.dbNotifChannel.id)
				.setContentTitle("Initialising download")
				.setContentText("Downloading $dbToDownloadString database for Bus2Go")
				.setSmallIcon(R.drawable.baseline_update)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setOngoing(false)
				.setProgress(-1, -1, true)
				.build()
		)
	}

	override suspend fun doWork(): Result {
		Log.d("DB WORKER", "Started db download work")

		//TODO notify when download starts (and show a bar perhaps)
		// once download done, notify for decompressing
		// finally notify with the priority high that Download Complete
		// and in notifications, show a "tap to restart"
		return withContext(Dispatchers.IO) {
			try {
				databaseAgency = DatabaseAgency.getEntry(dbToDownloadString)
				setForeground(getForegroundInfo())
				downloadDb()
			}
			catch (e: Exception) {
				Log.e("DB_WORKER", "An exception occurred...\n ${e.message}")
				withContext(Dispatchers.Main){
					notificationsRepository.notifyDbUpdates(NotificationType.DbUpdateError(e.message), databaseAgency)
				}
				Result.failure()
			}
		}
	}

	private suspend fun downloadDb(): Result {
		//TODO careful with this part, as a possible race condition may occur if at the same time we
		// are decompressing from already downloaded file
		//TODO check if this flow call actually works
		val serverChoice = settingsRepository.serverChoice.first()
		return when (val res = dbDownloadRepository.getDbUpToDateVersion(serverChoice, databaseAgency)){
			is Bus2GoResult.Error -> throw NetworkException(res.message)
			is Bus2GoResult.Success<Int> -> {
				if (!isAppUpToDate()){
					Log.d("DB_WORKER", "App version not up to date with database")
					withContext(Dispatchers.Main){
						notificationsRepository.notifyDbUpdates(NotificationType.DbUpdateError(), databaseAgency)
					}
					Result.failure()
				}
				//TODO in another worker, notify when not connected and only compressed file exists
				// (so that they click on "download" to decompress it)
				val currDbVersion = appStateRepository.getDatabaseVersion(databaseAgency)

				//the == is important in the case where the file already exists
				//instead of right away decompressing the file, we need to make sure the db doesn't already exist...
				//FIXMe for now, we will do the replacement cause fuck it i want it to work, user does it explicitly
				if (res.data >= currDbVersion) {
					val dbName = when(databaseAgency){
						DatabaseAgency.STM -> dbDownloadRepository.DB_NAME_STM
						DatabaseAgency.EXO -> dbDownloadRepository.DB_NAME_EXO
					}

					if (appStateRepository.doesUpToDateCompressedDbExist(databaseAgency, res.data) == null){
						dbDownloadRepository.downloadDb(serverChoice, databaseAgency, res.data)
							.collect { progress ->
							when(progress) {
								is Progress.Downloading -> {
									withContext(Dispatchers.Main){
										setProgress(
											workDataOf(
												KEY to databaseAgency.name.uppercase(),
											WORK_PROGRESS_CURRENT to progress.current,
												WORK_PROGRESS_MAX to progress.contentLength,
												WORK_PROGRESS_IS_DECOMPRESSING to false
											)
										)
										notificationsRepository.notifyDbUpdates(
											NotificationType.DbDownloading(
												progress.current,
												progress.contentLength
											),
											databaseAgency
										)
									}
								}
								Progress.Idle -> {}
								is Progress.Completed -> {
									if (!progress.success){
										withContext(Dispatchers.Main) {
											notificationsRepository.notifyDbUpdates(NotificationType.DbUpdateError(), databaseAgency)
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
										notificationsRepository.notifyDbUpdates(NotificationType.DbUpdateError(), databaseAgency)
									}
									Result.retry()
								}
							}
						}
					}
					appStateRepository.updateDatabaseVersion(databaseAgency, res.data)
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
									setProgress(
										workDataOf(
											KEY to databaseAgency.name.uppercase(),
											WORK_PROGRESS_IS_DECOMPRESSING to true
										)
									)
									notificationsRepository.notifyDbUpdates(NotificationType.DbExtracting, databaseAgency)
								}
							}
							is Progress.Completed -> {
								withContext(Dispatchers.Main) {
									notificationsRepository.notifyDbUpdates(NotificationType.DbUpdateDone, databaseAgency)
								}
							}
							else -> throw IllegalStateException("Wtf")
						}
					}
					appStateRepository.setRestartNeededFlag(databaseAgency)
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
	 * Checks whether the current version code is up to date with the database hosted in
	 * backend server (newer database may be incompatible with older versions of the app).
	 * */
	private suspend fun isAppUpToDate(): Boolean {
		//TODO also check if the version is smaller than the max accepted version
		//TODO verify flow.first works properly
		return when(val resp = dbDownloadRepository.getAppVersionCodeRequired(
			settingsRepository.serverChoice.first()
		)){
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