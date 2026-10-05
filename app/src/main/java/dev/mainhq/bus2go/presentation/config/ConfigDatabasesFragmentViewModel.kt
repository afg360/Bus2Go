package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.DatabaseState
import dev.mainhq.bus2go.domain.use_case.CancelDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.ObserveDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConfigDatabasesFragmentViewModel(
	observeDownloadDatabaseTask: ObserveDownloadDatabaseTask,
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask,
	private val cancelDownloadDatabaseTask: CancelDownloadDatabaseTask
): ViewModel() {

	@OptIn(ExperimentalCoroutinesApi::class)
	val databases = observeDownloadDatabaseTask.invoke().stateIn(
		viewModelScope,
		SharingStarted.WhileSubscribed(5000),
		emptyList()
	)

	fun downloadDatabase(databaseAgency: DatabaseAgency){
		viewModelScope.launch {
			scheduleDownloadDatabaseTask.invoke(databaseAgency)
		}
	}

	fun cancelDatabaseDownload(databaseAgency: DatabaseAgency){
		viewModelScope.launch {
			cancelDownloadDatabaseTask.invoke(databaseAgency)
		}
	}
}