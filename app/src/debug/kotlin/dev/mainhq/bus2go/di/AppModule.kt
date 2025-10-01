package dev.mainhq.bus2go.di

import android.content.Context
import androidx.work.WorkManager
import dev.mainhq.bus2go.BuildConfig
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.data.backgroundtask.DatabaseDownloadSchedulerImpl
import dev.mainhq.bus2go.data.repository.DatabaseDownloadRepositoryImpl
import dev.mainhq.bus2go.domain.use_case.settings.CheckIsBus2GoServer
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask

class AppModule(applicationContext: Context) {

	private val databaseDownloadScheduler = DatabaseDownloadSchedulerImpl(
		WorkManager.getInstance(applicationContext)
	)

	val dbDownloadRepository = DatabaseDownloadRepositoryImpl(
		//may be defined during runtime (from settings in case it is customised)
		BuildConfig.LOCAL_HOST,
		applicationContext as Bus2GoApplication,
		applicationContext.commonModule.networkMonitor,
		applicationContext.commonModule.notificationsRepository,
		applicationContext.commonModule.loggerImpl
	)

	val checkIsBus2GoServer = CheckIsBus2GoServer(dbDownloadRepository)

	val scheduleDownloadDatabaseTask = ScheduleDownloadDatabaseTask(
		databaseDownloadScheduler,
	)
}