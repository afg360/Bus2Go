package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.UrlChecker
import dev.mainhq.bus2go.domain.use_case.CheckIsBus2GoServer
import dev.mainhq.bus2go.domain.use_case.SaveBus2GoServer
import dev.mainhq.bus2go.presentation.core.UiState
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//TODO will eventually need a use case to handle a list of available public servers...

class ConfigServerFragmentViewModel(
	private val checkIsBus2GoServer: CheckIsBus2GoServer,
	private val saveBus2GoServer: SaveBus2GoServer
): ViewModel() {


	private val _buttonText = MutableStateFlow("Skip")
	val buttonText = _buttonText.asStateFlow()

	//store 1 element
	private val _textInputText: MutableSharedFlow<UiState<String>> = MutableSharedFlow(replay = 1)
	val textInputText = _textInputText.asSharedFlow()

	private val _serverResponse: MutableStateFlow<UiState<Boolean>> = MutableStateFlow(UiState.Init)
	val serverResponse = _serverResponse.asStateFlow()

	private val _savingServerState: MutableStateFlow<UiState<String>> = MutableStateFlow(UiState.Init)
	val savingServerState = _savingServerState.asStateFlow()

	private var job: Job? = null


	fun setServer(potentialUrl: String){
		//FIXME do some user input handling here
		//TODO verify if the user added an "http[s]://" thingy and whatnot
		job?.cancel()
		_serverResponse.update { UiState.Init }
		val config = UrlChecker.check(potentialUrl)
		viewModelScope.launch(Dispatchers.Main) {
			if (config == null) {
				_buttonText.update { "Skip" }
				_textInputText.emit(UiState.Error(""))
			}
			else if (config.data.isEmpty()) {
				_buttonText.update { "Skip" }
				_textInputText.emit(UiState.Success(""))
			}
			else {
				_buttonText.update {  "Continue" }
				_textInputText.emit(UiState.Success(config.data))
			}
		}
	}

	/** Called when the user clicks on Continue after having written a potential bsu2go server */
	fun checkIsBus2GoServer() {
		//before doing this shit, check if _serverResponse is already in success mode from the previous data...
		//if it is, no need to check back
		_serverResponse.update { UiState.Loading }

		job = viewModelScope.launch(Dispatchers.Main) {
			//capture the textInputText
			val value = _textInputText.first()
			if (value.instanceOf(UiState.Error::class)) _serverResponse.update { UiState.Error("") }
			val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
				throwable.printStackTrace()
			}
			withContext(Dispatchers.IO + coroutineExceptionHandler) {
				val result = checkIsBus2GoServer.invoke((value as UiState.Success).data)
				//if message is null from server success, then show invalid
				withContext(Dispatchers.Main) {
					when (result) {
						is Result.Error -> {
							_buttonText.update { "Skip" }
							_serverResponse.value =
								if (result.message != null) UiState.Error(result.message)
								else UiState.Error("")
						}

						is Result.Success<Boolean> -> {
							if (result.data){
								_buttonText.update { "Continue" }
								//FIXME for the moment ignore success/failure status
								saveBus2GoServer.invoke(value.data)
							}
							 else _buttonText.update { "Skip" }
							_serverResponse.update { UiState.Success(result.data) }
						}
					}
				}
			}
		}
	}

	fun cancel(){
		job?.cancel()
		_serverResponse.update { UiState.Init }
	}
}