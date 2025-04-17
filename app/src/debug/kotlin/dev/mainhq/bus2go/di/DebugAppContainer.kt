package dev.mainhq.bus2go.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.data.repository.DebugDbDownloadRepositoryImpl
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask

class DebugAppContainer(applicationContext: Context): AppContainer(applicationContext) {

	val dbDownloadRepository = DebugDbDownloadRepositoryImpl(
		WorkManager.getInstance(applicationContext)
	)

	val scheduleDownloadDatabaseTask = ScheduleDownloadDatabaseTask(
		dbDownloadRepository
	)
}