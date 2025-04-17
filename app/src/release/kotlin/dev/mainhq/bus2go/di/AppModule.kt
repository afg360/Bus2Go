package dev.mainhq.bus2go.di

import android.content.Context
import androidx.work.WorkManager
import dev.mainhq.bus2go.data.repository.DbDownloadRepositoryImpl
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask

class AppModule(applicationContext: Context){

	private val dbDownloadRepository = DbDownloadRepositoryImpl(
		WorkManager.getInstance(applicationContext)
	)

	val scheduleDownloadDatabaseTask = ScheduleDownloadDatabaseTask(
		dbDownloadRepository
	)
}