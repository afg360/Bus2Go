package dev.mainhq.bus2go.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import kotlinx.coroutines.launch

class SettingsUpdateFragmentViewModel(
	private val downloadDatabaseTask: ScheduleDownloadDatabaseTask,
): ViewModel() {

	fun downloadStm(){
		viewModelScope.launch {
			downloadDatabaseTask.invoke(DbToDownload.STM)
		}
	}

	fun downloadExo(){
		viewModelScope.launch {
			downloadDatabaseTask.invoke(DbToDownload.EXO)
		}
	}
}