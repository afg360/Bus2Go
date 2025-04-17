package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import kotlinx.coroutines.launch

class ConfigDatabasesFragmentViewModel(
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask
): ViewModel() {

	fun download(dbToDownload: DbToDownload){
		viewModelScope.launch {
			scheduleDownloadDatabaseTask.invoke(dbToDownload)
		}
	}
}