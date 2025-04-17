package dev.mainhq.bus2go.di

import android.content.Context
import androidx.work.WorkManager
import dev.mainhq.bus2go.data.repository.DebugDbDownloadRepositoryImpl
import dev.mainhq.bus2go.domain.repository.DbDownloadRepository
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask

class AppModule(applicationContext: Context) {

	private val dbDownloadRepository: DbDownloadRepository = DebugDbDownloadRepositoryImpl(
		WorkManager.getInstance(applicationContext)
	)

	val scheduleDownloadDatabaseTask = ScheduleDownloadDatabaseTask(
		dbDownloadRepository
	)
}