package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.workDataOf
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConfigDatabasesFragmentViewModel(
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask
): ViewModel() {

	private val _dbToDownload: MutableStateFlow<Set<DbToDownload>> = MutableStateFlow(mutableSetOf())
	val dbToDownload = _dbToDownload.asStateFlow()

	fun toggleStm(){
		_dbToDownload.update {
			val newSet = it.toMutableSet()
			if (it.contains(DbToDownload.STM)){
				newSet.remove(DbToDownload.STM)
				newSet
			}
			else {
				newSet.add(DbToDownload.STM)
			}
			newSet
		}
	}

	fun toggleExo(){
		_dbToDownload.update {
			val newSet = it.toMutableSet()
			if (it.contains(DbToDownload.EXO)){
				newSet.remove(DbToDownload.EXO)
			}
			else {
				newSet.add(DbToDownload.EXO)
			}
			newSet
		}
	}

	fun isStmChecked(): Boolean {
		return _dbToDownload.value.contains(DbToDownload.STM)
	}

	fun isExoChecked(): Boolean {
		return _dbToDownload.value.contains(DbToDownload.EXO)
	}

	fun scheduleDownloadWork(){
		if (_dbToDownload.value.isNotEmpty()) {
			viewModelScope.launch {
				scheduleDownloadDatabaseTask.invoke(_dbToDownload.value.toList())
			}
		}
	}
}