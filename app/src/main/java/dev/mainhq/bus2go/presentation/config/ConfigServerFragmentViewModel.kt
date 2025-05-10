package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import dev.mainhq.bus2go.domain.entity.ServerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

//TODO will eventually need a use case to handle a list of available public servers...

class ConfigServerFragmentViewModel: ViewModel() {

	//private val _server: MutableStateFlow<UiState<String>> = MutableStateFlow(UiState.Success(""))

	private val _buttonText = MutableStateFlow("Skip")
	val buttonText = _buttonText.asStateFlow()

	private val _textInputText = MutableStateFlow("")
	val textInputText = _textInputText.asStateFlow()


	fun setServer(server: String){
		//FIXME do some user input handling here
		//TODO verify if the user added an "http[s]://" thingy and whatnot
		val config = ServerConfig.build(server)
		if (config == null) {
			_buttonText.value = "Skip"
			_textInputText.value = ""
		}
		else if (config.data.isEmpty()) {
			_buttonText.value = "Skip"
			_textInputText.value = ""
		}
		else {
			_buttonText.value = "Continue"
			_textInputText.value = config.data
		}
	}
}