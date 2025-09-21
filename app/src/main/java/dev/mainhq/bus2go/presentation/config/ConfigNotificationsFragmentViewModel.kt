package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.use_case.SaveAllNotifSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConfigNotificationsFragmentViewModel(
	private val saveAllNotifSettings: SaveAllNotifSettings
): ViewModel() {

	private val _appUpdateNotifs = MutableStateFlow(false)
	val appUpdateNotifs = _appUpdateNotifs.asStateFlow()

	private val _dbUpdateNotifs = MutableStateFlow(false)
	val dbUpdateNotifs = _dbUpdateNotifs.asStateFlow()

	fun setAppUpdateNotifs(boolean: Boolean){
		_appUpdateNotifs.update { boolean }
	}

	fun setDbUpdateNotifs(boolean: Boolean){
		_dbUpdateNotifs.update { boolean }
	}

	fun anyNotifSet(): Boolean{
		return _appUpdateNotifs.value || _dbUpdateNotifs.value
	}

	fun saveSettings() {
		//FIXME ignoring return value for now...
		// perhaps notify when a result is given and do something with it
		viewModelScope.launch {
			saveAllNotifSettings.invoke(_appUpdateNotifs.value, _dbUpdateNotifs.value)
		}
	}
}