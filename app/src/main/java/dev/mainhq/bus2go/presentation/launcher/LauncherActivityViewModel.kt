package dev.mainhq.bus2go.presentation.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.use_case.db_state.IsFirstTimeAppLaunched
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LauncherActivityViewModel(
	private val isFirstTimeAppLaunchedUseCase: IsFirstTimeAppLaunched
): ViewModel() {

	//set to null so that we wait for the actual value
	//TODO instead use a cold flow and collect its emission
	private val _isFirstTime: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isFirstTime = _isFirstTime.asStateFlow()

	init{
		viewModelScope.launch {
			_isFirstTime.update { isFirstTimeAppLaunchedUseCase() }
		}
	}
}