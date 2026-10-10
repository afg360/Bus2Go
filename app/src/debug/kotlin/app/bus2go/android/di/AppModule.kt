package app.bus2go.android.di

import android.content.Context
import androidx.work.WorkManager
import app.bus2go.android.BuildConfig
import app.bus2go.android.Bus2GoApplication
import app.bus2go.android.data.backgroundtask.DatabaseDownloadSchedulerImpl
import app.bus2go.android.data.repository.DebugDatabaseDownloadRepositoryImpl
import app.bus2go.android.domain.use_case.ObserveDownloadDatabaseTask
import app.bus2go.android.domain.use_case.settings.CheckIsBus2GoServer
import app.bus2go.android.domain.use_case.ScheduleDownloadDatabaseTask

class AppModule(applicationContext: Context) {

	val loggerImpl = LoggerImpl()
	val networkMonitor = NetworkMonitor.getInstance(applicationContext, loggerImpl)

	val dbDownloadRepository = DebugDatabaseDownloadRepositoryImpl(
		//may be defined during runtime (from settings in case it is customised)
		BuildConfig.LOCAL_HOST,
		applicationContext.filesDir,
		networkMonitor,
		loggerImpl
	)

	val checkIsBus2GoServer = CheckIsBus2GoServer(dbDownloadRepository)
}