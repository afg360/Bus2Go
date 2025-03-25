package dev.mainhq.bus2go.presentation.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/** ViewModel that saves the state of the last fragment attached to the configurationActivity, and notifies
 * 	the activity when configuration is done */
class ConfigurationStateViewModel(application: Application) : AndroidViewModel(application) {

	private val _currentFragmentTag: MutableLiveData<String?> = MutableLiveData()
	val currentFragmentTag: LiveData<String?> get() = _currentFragmentTag

	private val _event = MutableLiveData(false)
	val event: LiveData<Boolean> get() = _event

	fun triggerEvent(startActivity: Boolean) {
		_event.value = startActivity
		_currentFragmentTag.value = null
	}

	fun setFragmentTag(tag: String){
		//_currentFragmentId.value = id
		_currentFragmentTag.value = tag
	}
}