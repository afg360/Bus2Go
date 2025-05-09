package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.workDataOf
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfigDatabasesFragmentViewModel(
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask
): ViewModel() {

	private val _dbToDownload: MutableStateFlow<DbToDownload?> = MutableStateFlow(DbToDownload.STM)
	val dbToDownload = _dbToDownload.asStateFlow()

	fun toggleStm(){
		_dbToDownload.value = when (_dbToDownload.value){
			DbToDownload.ALL -> DbToDownload.EXO
			DbToDownload.STM -> null
			DbToDownload.EXO -> DbToDownload.ALL
			null -> DbToDownload.STM
		}
	}

	fun toggleExo(){
		_dbToDownload.value = when (_dbToDownload.value){
			DbToDownload.ALL -> DbToDownload.STM
			DbToDownload.STM -> DbToDownload.ALL
			DbToDownload.EXO -> null
			null -> DbToDownload.EXO
		}
	}

	fun isStmChecked(): Boolean{
		return _dbToDownload.value == DbToDownload.ALL || _dbToDownload.value == DbToDownload.STM
	}

	fun isExoChecked(): Boolean{
		return _dbToDownload.value == DbToDownload.ALL || _dbToDownload.value == DbToDownload.EXO
	}

	fun scheduleDownloadWork(){
		if (_dbToDownload.value == null)
			TODO("Wtf...")
		viewModelScope.launch {
			scheduleDownloadDatabaseTask.invoke(_dbToDownload.value!!)
		}
	}
}