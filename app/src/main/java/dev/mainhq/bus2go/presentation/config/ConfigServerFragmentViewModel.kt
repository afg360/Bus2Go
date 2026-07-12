package dev.mainhq.bus2go.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ServerChoice
import dev.mainhq.bus2go.domain.entity.UrlChecker
import dev.mainhq.bus2go.domain.exceptions.UnpinnedCertificateException
import dev.mainhq.bus2go.domain.repository.SettingsRepository
import dev.mainhq.bus2go.domain.use_case.AcceptSelfSignedCertificate
import dev.mainhq.bus2go.domain.use_case.settings.CheckIsBus2GoServer
import dev.mainhq.bus2go.domain.use_case.settings.SaveAllNotifSettings
import dev.mainhq.bus2go.presentation.core.UiState
import dev.mainhq.bus2go.utils.findCause
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ConfigServerFragmentViewModel(
	private val settingsRepository: SettingsRepository,
	private val checkIsBus2GoServer: CheckIsBus2GoServer,
	private val saveAllNotifSettings: SaveAllNotifSettings,
	private val acceptSelfSignedCertificate: AcceptSelfSignedCertificate
): ViewModel(), SettingsSavableViewModel {

	private val _buttonText = MutableStateFlow("Skip")
	val buttonText = _buttonText.asStateFlow()

	//store 1 element
	private val _textInputText: MutableStateFlow<UiState<String>> = MutableStateFlow(UiState.Init)
	val textInputText = _textInputText.asStateFlow()

	private val _serverResponse: MutableStateFlow<UiState<Boolean>> = MutableStateFlow(UiState.Init)
	val serverResponse = _serverResponse.asStateFlow()

	fun setServer(potentialUrl: String){
		//FIXME do some user input handling here
		//TODO verify if the user added an "http[s]://" thingy and whatnot
		_serverResponse.update { UiState.Init }
		val config = UrlChecker.check(potentialUrl)
		if (config == null) {
			_buttonText.update { "Skip" }
			_textInputText.update { UiState.Error("") }
		}
		else if (config.data.isEmpty()) {
			_buttonText.update { "Skip" }
			_textInputText.update { UiState.Success("") }
		}
		else {
			_buttonText.update { "Continue" }
			_textInputText.update { UiState.Success(config.data) }
		}
	}


	private val _warnUser: MutableSharedFlow<WarningType> = MutableSharedFlow(replay = 1)
	val warnUser = _warnUser.asSharedFlow()

	/** Called when the user clicks on Continue after having written a potential bsu2go server */
	fun verifyUserInputServer() {
		viewModelScope.launch(Dispatchers.Main) {
			//before doing this shit, check if _serverResponse is already in success mode from the previous data...
			//if it is, no need to check back
			_serverResponse.update { UiState.Loading }
			//capture the textInputText
			if (_textInputText.value.instanceOf(UiState.Error::class)) {
				_serverResponse.update { UiState.Error("Invalid URL") }
			}

			val inputTextString = (_textInputText.value as UiState.Success).data
			when(_serverType.value) {
				ServerType.SELF_HOSTED -> {
					_warnUser.emit(WarningType.QuerySelfHosted)
				}
				ServerType.WEB -> {
					val result = checkIsBus2GoServer.invoke(inputTextString, _serverType.value)
					_checkIsBus2Go(inputTextString, result)
				}
			}

		}
	}


	/** Called once the user accepts to query a self hosted potential bus2go server */
	fun checkIsBus2Go() {
		assert(_serverType.value == ServerType.SELF_HOSTED)
		viewModelScope.launch {
			val inputTextString = (_textInputText.value as UiState.Success).data
			val result = checkIsBus2GoServer.invoke(inputTextString, _serverType.value)
			when (result) {
				is Result.Error -> {
					val exception = result.throwable?.findCause<UnpinnedCertificateException>()
					if (exception != null) {
						_warnUser.emit(WarningType.AcceptSelfSignedCertificate(exception.certificate))
					}
					else {
						//TODO put "Skip option"
						_serverResponse.update { UiState.Error("Unexpected Error occurred") }
					}
				}
				else -> _checkIsBus2Go(inputTextString, result)
			}
		}
	}

	fun acceptCert() {
		viewModelScope.launch {
			when(val throwable = _warnUser.first()) {
				WarningType.QuerySelfHosted -> throw Exception("Wtf")
				is WarningType.AcceptSelfSignedCertificate -> {
					acceptSelfSignedCertificate.invoke(throwable.cert)
				}
			}
			checkIsBus2Go()
		}
	}

	private fun _checkIsBus2Go(inputTextString: String, result: Result<Boolean>) {
		//if message is null from server success, then show invalid
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
					//FIXME need to pass down a correct ServerChoice
					viewModelScope.launch {
						settingsRepository.setBus2GoServer(ServerChoice(inputTextString, true))
					}
				}
				else {
					_buttonText.update { "Skip" }
				}
				_serverResponse.update { UiState.Success(result.data) }
			}
		}
	}

	private val _serverType = MutableStateFlow(ServerType.SELF_HOSTED)
	val serverType = _serverType.asStateFlow()

	fun toggleServerType() {
		_serverType.update { !_serverType.value }
	}

	override fun saveSettings() {
		viewModelScope.launch(Dispatchers.Main) {
			saveAllNotifSettings.invoke()
		}
	}

	fun cancelQuery(){
		_serverResponse.update { UiState.Init }
	}
}