package app.bus2go.android.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bus2go.android.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfigThemeFragmentViewModel(
	private val settingsRepository: SettingsRepository,
): ViewModel() {

	val isDarkMode = settingsRepository.isDarkMode
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			true
		)

	fun toggleDarkMode(){
		viewModelScope.launch {
			settingsRepository.setIsDarkMode(!isDarkMode.value)
		}
	}

}