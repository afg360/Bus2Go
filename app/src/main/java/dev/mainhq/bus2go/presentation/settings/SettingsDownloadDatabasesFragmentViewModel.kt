package dev.mainhq.bus2go.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.use_case.CancelDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.ObserveDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.db_state.DeleteDatabase
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Should be an activity ViewModel since it should still run in the background */
class SettingsDownloadDatabasesFragmentViewModel(
	observeDownloadDatabaseTask: ObserveDownloadDatabaseTask,
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask,
	private val cancelDownloadDatabaseTask: CancelDownloadDatabaseTask,
	private val deleteDatabase: DeleteDatabase
): ViewModel() {

	@OptIn(ExperimentalCoroutinesApi::class)
	val databases = observeDownloadDatabaseTask.invoke()
		.map {
			UiState.Success(it)
		}
		.stateIn(
		viewModelScope,
		SharingStarted.WhileSubscribed(5000),
			UiState.Init
	)

	fun downloadDatabase(databaseAgency: DatabaseAgency) {
		viewModelScope.launch {
			scheduleDownloadDatabaseTask.invoke(databaseAgency)
		}
	}

	fun cancelDownload(databaseAgency: DatabaseAgency) {
		viewModelScope.launch {
			cancelDownloadDatabaseTask.invoke(databaseAgency)
		}
	}

	fun deleteDatabase(databaseAgency: DatabaseAgency) {
		viewModelScope.launch {
			deleteDatabase.invoke(databaseAgency)
		}
	}

}