package dev.mainhq.bus2go.presentation.config

import android.app.Application
import android.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** ViewModel that saves the state of the last fragment attached to the configurationActivity, and notifies
 * 	the activity when configuration is done */
class ConfigSharedViewModel : ViewModel() {

	private val _currentFragment: MutableStateFlow<FragmentUsed?> = MutableStateFlow(null)
	val currentFragment = _currentFragment.asStateFlow()

	private val _event = MutableStateFlow(false)
	val event = _event.asStateFlow()

	fun triggerEvent(startActivity: Boolean) {
		_event.value = startActivity
		_currentFragment.value = null
	}

	fun setFragment(fragment: FragmentUsed){
		//_currentFragmentId.value = id
		_currentFragment.value = fragment
	}
}