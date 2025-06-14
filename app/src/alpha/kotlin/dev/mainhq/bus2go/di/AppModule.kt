package dev.mainhq.bus2go.di

import android.content.Context
import androidx.work.WorkManager
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.domain.use_case.CheckIsBus2GoServer
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import dev.mainhq.bus2go.data.backgroundtask.DatabaseDownloadSchedulerImpl
import dev.mainhq.bus2go.data.repository.DatabaseDownloadRepositoryImpl

class AppModule(applicationContext: Context) {
	private val databaseDownloadScheduler = DatabaseDownloadSchedulerImpl(
		WorkManager.getInstance(applicationContext)
	)

	val dbDownloadRepository = DatabaseDownloadRepositoryImpl(
		applicationContext as Bus2GoApplication,
		applicationContext.commonModule.networkMonitor,
		applicationContext.commonModule.notificationsRepository,
		applicationContext.commonModule.settingsRepository
	)

	val checkIsBus2GoServer = CheckIsBus2GoServer(dbDownloadRepository)

	val scheduleDownloadDatabaseTask = ScheduleDownloadDatabaseTask(
		databaseDownloadScheduler,
	)

}