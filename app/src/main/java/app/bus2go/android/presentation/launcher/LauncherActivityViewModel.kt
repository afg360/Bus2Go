package app.bus2go.android.presentation.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bus2go.android.domain.repository.SettingsRepository
import app.bus2go.android.domain.use_case.db_state.IsFirstTimeAppLaunched
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class LauncherActivityViewModel(
	settingsRepository: SettingsRepository,
	private val isFirstTimeAppLaunchedUseCase: IsFirstTimeAppLaunched,
): ViewModel() {

	val isFirstTime = flow {
		emit(isFirstTimeAppLaunchedUseCase.invoke())
	}.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), null)

	val isDarkMode = settingsRepository.isDarkMode
		.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), null)

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