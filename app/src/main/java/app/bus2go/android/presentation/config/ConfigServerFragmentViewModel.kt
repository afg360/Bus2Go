package app.bus2go.android.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bus2go.android.domain.core.Result
import app.bus2go.android.domain.entity.ServerChoice
import app.bus2go.android.domain.entity.UrlChecker
import app.bus2go.android.domain.exceptions.ExpiredCertificateException
import app.bus2go.android.domain.exceptions.UnpinnedCertificateException
import app.bus2go.android.domain.repository.SettingsRepository
import app.bus2go.android.domain.use_case.AcceptSelfSignedCertificate
import app.bus2go.android.domain.use_case.DeleteSelfSignedCertificates
import app.bus2go.android.domain.use_case.settings.CheckIsBus2GoServer
import app.bus2go.android.domain.use_case.settings.SaveAllNotifSettings
import app.bus2go.android.presentation.core.UiState
import app.bus2go.android.utils.findCause
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.cert.X509Certificate

class ConfigServerFragmentViewModel(
	private val settingsRepository: SettingsRepository,
	private val checkIsBus2GoServer: CheckIsBus2GoServer,
	private val saveAllNotifSettings: SaveAllNotifSettings,
	private val acceptSelfSignedCertificate: AcceptSelfSignedCertificate,
	private val deleteSelfSignedCertificates: DeleteSelfSignedCertificates
): ViewModel(), SettingsSavableViewModel {

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
			_textInputText.update { UiState.Error("") }
		}
		else if (config.data.isEmpty()) {
			_textInputText.update { UiState.Success("") }
		}
		else {
			_textInputText.update { UiState.Success(config.data) }
		}
	}

	private val _certificate: MutableStateFlow<X509Certificate?> = MutableStateFlow(null)
	private val _warnUser: MutableSharedFlow<WarningType> = MutableSharedFlow()
	val warnUser = _warnUser.asSharedFlow()

	/** Called when the user clicks on Continue after having written a potential bsu2go server */
	fun verifyUserInputServer() {
		viewModelScope.launch(Dispatchers.Main) {
			//before doing this shit, check if _serverResponse is already in success mode from the previous data...
			//if it is, no need to check back
			_serverResponse.update { UiState.Loading }
			//capture the textInputText
			if (_textInputText.value is UiState.Error) {
				_serverResponse.update { UiState.Error("Invalid URL") }
			}

			when(_serverType.value) {
				ServerType.SELF_HOSTED -> {
					_warnUser.emit(WarningType.QuerySelfHosted)
				}
				ServerType.WEB -> {
					_certificate.update { null }
					deleteSelfSignedCertificates.invoke()
					val inputTextString = (_textInputText.value as UiState.Success).data
					when (val result = checkIsBus2GoServer.invoke(inputTextString, _serverType.value)) {
						is Result.Error -> {
							_serverResponse.update { UiState.Error("Unexpected Error occurred: ${result.throwable}") }
						}
						is Result.Success<Boolean> -> {
							//if message is null from server success, then show invalid
							val serverChoice = ServerChoice(inputTextString, true)
							if (result.data) {
								viewModelScope.launch {
									settingsRepository.setBus2GoServer(serverChoice)
								}
							}
							_serverResponse.update { UiState.Success(result.data) }
						}
					}
				}
			}

		}
	}


	/** Called once the user accepts to query a self-hosted potential bus2go server */
	fun checkIsBus2Go() {
		//TODO test this with online VPS!!!
		viewModelScope.launch {
			val inputTextString = (_textInputText.value as UiState.Success).data
			when (val result = checkIsBus2GoServer.invoke(inputTextString, _serverType.value)) {
				is Result.Error -> {
					if (result.throwable?.findCause<UnpinnedCertificateException>() != null) {
						val certificate = result.throwable.findCause<UnpinnedCertificateException>()!!.certificate
						_certificate.update { certificate }
						_warnUser.emit(WarningType.AcceptSelfSignedCertificate(certificate))
					}
					else if (result.throwable?.findCause<ExpiredCertificateException>() != null) {
						_certificate.update { null }
						deleteSelfSignedCertificates.invoke()
						_serverResponse.update { UiState.Error("Server SSL certificate is expired") }
					}
					else {
						//TODO put "Skip option"
						_certificate.update { null }
						deleteSelfSignedCertificates.invoke()
						_serverResponse.update { UiState.Error("Unexpected Error occurred") }
					}
				}
				is Result.Success<Boolean> -> {
					//if message is null from server success, then show invalid
					val serverChoice = ServerChoice(inputTextString, true)
					_certificate.update { null }
					deleteSelfSignedCertificates.invoke()
					if (result.data) {
						viewModelScope.launch {
							settingsRepository.setBus2GoServer(serverChoice)
						}
					}
					_serverResponse.update { UiState.Success(result.data) }
				}
			}
		}
	}

	fun acceptCert() {
		viewModelScope.launch {
			val cert = _certificate.value
			if (cert != null) {
				acceptSelfSignedCertificate.invoke(cert)
				checkIsBus2Go()
			}
			else {
				_textInputText.update { UiState.Error("Wtf") }
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