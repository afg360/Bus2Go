package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConfigDatabasesFragmentViewModel(
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask
): ViewModel() {

	private val _databaseAgency: MutableStateFlow<Set<DatabaseAgency>> = MutableStateFlow(mutableSetOf())
	val dbToDownload = _databaseAgency.asStateFlow()

	fun toggleStm(){
		_databaseAgency.update {
			val newSet = it.toMutableSet()
			if (it.contains(DatabaseAgency.STM)){
				newSet.remove(DatabaseAgency.STM)
				newSet
			}
			else {
				newSet.add(DatabaseAgency.STM)
			}
			newSet
		}
	}

	fun toggleExo(){
		_databaseAgency.update {
			val newSet = it.toMutableSet()
			if (it.contains(DatabaseAgency.EXO)){
				newSet.remove(DatabaseAgency.EXO)
			}
			else {
				newSet.add(DatabaseAgency.EXO)
			}
			newSet
		}
	}

	fun isStmChecked(): Boolean {
		return _databaseAgency.value.contains(DatabaseAgency.STM)
	}

	fun isExoChecked(): Boolean {
		return _databaseAgency.value.contains(DatabaseAgency.EXO)
	}

	fun scheduleDownloadWork(){
		if (_databaseAgency.value.isNotEmpty()) {
			viewModelScope.launch {
				scheduleDownloadDatabaseTask.invoke(_databaseAgency.value.toList())
			}
		}
	}
}