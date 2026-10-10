package app.bus2go.android.presentation.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsSharedViewModel: ViewModel() {

	private val _fragmentUsed = MutableStateFlow(FragmentUsed.MAIN)
	val fragmentUsed = _fragmentUsed.asStateFlow()

	private val _isLoading = MutableStateFlow(false)
	val isLoading = _isLoading.asStateFlow()

	fun setFragment(fragmentUsed: FragmentUsed){
		_fragmentUsed.update { fragmentUsed }
	}

	fun setLoading(loading: Boolean){
		_isLoading.update { loading }
	}

}
