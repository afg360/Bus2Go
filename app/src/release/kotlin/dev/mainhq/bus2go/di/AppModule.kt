package dev.mainhq.bus2go.di

import android.content.Context
import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.data.repository.DatabaseDownloadRepositoryImpl
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.use_case.settings.CheckIsBus2GoServer

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