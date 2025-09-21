package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfigThemeFragmentViewModel: ViewModel() {

	private val _darkMode = MutableStateFlow(true)
	val darkMode = _darkMode.asStateFlow()

	fun setDarkMode(bool: Boolean){
		//TODO deal with setting the whole app in light/dark mode here and by using a useCase
		_darkMode.update { bool }
	}

}