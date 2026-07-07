package dev.mainhq.bus2go.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
