package app.bus2go.android.presentation.config

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** ViewModel that saves the state of the last fragment attached to the configurationActivity, and notifies
 * 	the activity when configuration is done */
class ConfigSharedViewModel : ViewModel() {

	private val _currentFragment: MutableStateFlow<FragmentUsed> = MutableStateFlow(FragmentUsed.WELCOME)
	val currentFragment = _currentFragment.asStateFlow()

	private val _configButtonState: MutableStateFlow<ConfigButtonState> = MutableStateFlow(ConfigButtonState.Next{})
	val configButtonState = _configButtonState.asStateFlow()

	fun setFragment(fragment: FragmentUsed){
		_currentFragment.update { fragment }
	}

	fun setButtonState(configButtonState: ConfigButtonState) {
		_configButtonState.update { configButtonState }
	}

	fun nextFragment() {
		when(_currentFragment.value) {
			FragmentUsed.WELCOME -> {
				_currentFragment.update { FragmentUsed.THEME }
			}
			FragmentUsed.THEME -> TODO()
			FragmentUsed.NOTIFICATIONS -> TODO()
			FragmentUsed.SERVER -> TODO()
			FragmentUsed.DATABASES -> TODO()
		}
	}
}