package dev.mainhq.bus2go.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.data.worker.DatabaseDownloadManagerWorker

class WorkerFactory: WorkerFactory() {
	override fun createWorker(
		appContext: Context,
		workerClassName: String,
		workerParameters: WorkerParameters,
	): ListenableWorker? {
		return (appContext as Bus2GoApplication).let{
			when (workerClassName) {
				DatabaseDownloadManagerWorker::class.java.name ->
					DatabaseDownloadManagerWorker(it, workerParameters, it.commonModule.notificationsRepository)
				else -> null
			}
		}
	}
}