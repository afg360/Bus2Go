package dev.mainhq.bus2go.data.worker

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Worker downloading the selected databases at configuration time. */
class DatabaseDownloadManagerWorker(
	context: Context,
	workerParams: WorkerParameters,
	baseUrl: String? //must be passed by using settings, but settings require app launched???
) : CoroutineWorker(context, workerParams) {

	private lateinit var notificationManager: NotificationManager

	override suspend fun doWork(): Result {
		//check which databases to download
		val isStm = inputData.getBoolean("stm", false)
		val isExo = inputData.getBoolean("exo", false)
		val jobs: MutableList<Deferred<Boolean>> = mutableListOf()
		notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		Log.d("DB WORKER", "Started db download work")
		//FIXME downloading initiates but does not seem to save files properly...?
		if (withContext(Dispatchers.IO){
			//if (isStm) jobs.add(async{return@async downloadStmDb()})
			//if (isExo) downloadAllExo(this, jobs)

			return@withContext jobs.map { it.await() }.reduce{b1, b2 -> b1 && b2}
		})
			return Result.success()
		else return Result.failure()
	}

}