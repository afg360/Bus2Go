package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import dev.mainhq.bus2go.domain.use_case.SaveAllNotifSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfigNotificationsFragmentViewModel(
	private val saveAllNotifSettings: SaveAllNotifSettings
): ViewModel() {

	private val _appUpdateNotifs = MutableStateFlow(false)
	val appUpdateNotifs = _appUpdateNotifs.asStateFlow()

	private val _dbUpdateNotifs = MutableStateFlow(false)
	val dbUpdateNotifs = _dbUpdateNotifs.asStateFlow()

	fun setAppUpdateNotifs(boolean: Boolean){
		_appUpdateNotifs.value = boolean
	}

	fun setDbUpdateNotifs(boolean: Boolean){
		_dbUpdateNotifs.value = boolean
	}

	fun anyNotifSet(): Boolean{
		return _appUpdateNotifs.value || _dbUpdateNotifs.value
	}

	fun saveSettings(): Boolean {
		//FIXME ignoring return value for now...
		return saveAllNotifSettings.invoke(_appUpdateNotifs.value, _dbUpdateNotifs.value)
	}
}