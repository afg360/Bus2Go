package app.bus2go.android.di

import android.content.Context
import app.bus2go.android.data.data_source.remote.NetworkMonitor
import app.bus2go.android.data.repository.DatabaseDownloadRepositoryImpl
import app.bus2go.android.domain.core.Logger
import app.bus2go.android.domain.use_case.settings.CheckIsBus2GoServer

class AppModule(applicationContext: Context) {

	val loggerImpl: Logger? = null
	val networkMonitor = NetworkMonitor.getInstance(applicationContext, loggerImpl)

	val dbDownloadRepository = DatabaseDownloadRepositoryImpl(
		applicationContext,
		loggerImpl,
		networkMonitor,
	)

	val checkIsBus2GoServer = CheckIsBus2GoServer(dbDownloadRepository)
}