package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ServerConfig
import dev.mainhq.bus2go.domain.use_case.CheckIsBus2GoServer
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//TODO will eventually need a use case to handle a list of available public servers...

class ConfigServerFragmentViewModel(
	private val checkIsBus2GoServer: CheckIsBus2GoServer
): ViewModel() {


	private val _buttonText = MutableStateFlow("Skip")
	val buttonText = _buttonText.asStateFlow()

	private val _textInputText = MutableStateFlow("")
	val textInputText = _textInputText.asStateFlow()

	private val _serverResponse: MutableStateFlow<UiState<Boolean>> = MutableStateFlow(UiState.Init)
	val serverResponse = _serverResponse.asStateFlow()

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

	fun checkIsBus2GoServer() {
		//before doing this shit, check if _serverResponse is already in success mode from the previous data...
		//if it is, no need to check back
		_serverResponse.value = UiState.Loading

		//capture the textInputText
		val value = _textInputText.value
		if (value.isEmpty()) _serverResponse.value = UiState.Error("")

		viewModelScope.launch(Dispatchers.IO) {
			delay(3000)
			//TODO network call implementations
			val result = checkIsBus2GoServer.invoke(value)
			withContext(Dispatchers.Main) {
				when (result) {
					is Result.Error -> _serverResponse.value = UiState.Error("")
					is Result.Success<Boolean> -> {
						//TODO
						_serverResponse.value = UiState.Success(result.data)
					}
				}
			}

		}
	}
}