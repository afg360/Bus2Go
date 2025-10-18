package dev.mainhq.bus2go.presentation.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.use_case.db_state.IsFirstTimeAppLaunched
import dev.mainhq.bus2go.domain.use_case.settings.GetSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LauncherActivityViewModel(
	private val isFirstTimeAppLaunchedUseCase: IsFirstTimeAppLaunched,
	private val getSettings: GetSettings
): ViewModel() {

	val isFirstTime = flow {
		emit(isFirstTimeAppLaunchedUseCase.invoke())
	}.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), null)

	val isDarkMode = flow {
		emit(getSettings.invoke().isDarkMode)
	}.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), null)

	//using these instead of checking for null, because we want to make sure that every operation is
	// completed when reading the data before being sure to proceed to the next, not to proceed to the next
	// right when the data itself changes
	private val _isFirstTimeSet = MutableStateFlow(false)
	private val _isThemeSet = MutableStateFlow(false)

	fun firstThemeSet() = _isFirstTimeSet.update { true }
	fun themeSet() = _isThemeSet.update { true }

	val readyToFinish = combine(_isFirstTimeSet, _isThemeSet) { isFirstTimeSet, isThemeSet ->
		isFirstTimeSet && isThemeSet
	}.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), false)

}