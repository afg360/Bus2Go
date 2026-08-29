package dev.mainhq.bus2go.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsUpdatesFragmentViewModel(
	private val settingsRepository: SettingsRepository,
): ViewModel() {

	val isAppUpdatesNotifOn = settingsRepository.isAppUpdatesNotifOn
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			false
		)

	fun toggleIsAppUpdatesNotifOn() {
		viewModelScope.launch {
			settingsRepository.setAppUpdatesNotif(!isAppUpdatesNotifOn.value)
		}
	}

	val isAutoAppUpdatesOn = settingsRepository.isAutoAppUpdatesOn
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			false
		)

	fun toggleIsAutoAppUpdatesOn() {
		viewModelScope.launch {
			settingsRepository.setAutoAppUpdates(!isAutoAppUpdatesOn.value)
		}
	}

	val isDbUpdatesNotifOn = settingsRepository.isDbUpdatesNotifOn
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			false
		)

	fun toggleIsDbUpdatesNotifOn() {
		viewModelScope.launch {
			settingsRepository.setDbUpdatesNotif(!isDbUpdatesNotifOn.value)
		}
	}

	val isDbAutoUpdatesOn = settingsRepository.isDbAutoUpdatesOn
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			false
		)

	fun toggleIsDbAutoUpdatesOn() {
		viewModelScope.launch {
			settingsRepository.setDbAutoUpdates(!isDbAutoUpdatesOn.value)
		}
	}
}