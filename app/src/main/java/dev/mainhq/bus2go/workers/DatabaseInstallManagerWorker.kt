package dev.mainhq.bus2go.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DatabaseInstallManagerWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
	override suspend fun doWork(): Result {
		TODO("Not yet implemented")
	}
}