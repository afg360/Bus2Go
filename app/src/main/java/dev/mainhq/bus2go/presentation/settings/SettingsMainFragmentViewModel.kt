package dev.mainhq.bus2go.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ServerChoice
import dev.mainhq.bus2go.domain.exceptions.UnpinnedCertificateException
import dev.mainhq.bus2go.domain.repository.SettingsRepository
import dev.mainhq.bus2go.domain.use_case.settings.CheckIsBus2GoServer
import dev.mainhq.bus2go.presentation.config.ServerType
import dev.mainhq.bus2go.presentation.core.UiState
import dev.mainhq.bus2go.utils.findCause
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsMainFragmentViewModel(
	private val settingsRepository: SettingsRepository,
	private val checkIsBus2GoServer: CheckIsBus2GoServer,
): ViewModel() {

	private val _toastText = MutableSharedFlow<Response>(replay = 0)
	val toastText = _toastText.asSharedFlow()

	val langChoice = settingsRepository.lang
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			0
		)

	fun setLang(langPos: Int) {
		viewModelScope.launch {
			settingsRepository.setLang(langPos)
		}
	}

	val isDarkMode = settingsRepository.isDarkMode
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			true
		)

	fun toggleDarkMode() {
		viewModelScope.launch {
			settingsRepository.setIsDarkMode(!isDarkMode.value)
		}
	}

	val serverChoice = settingsRepository.serverChoice
		.map {
			if (it.server.isBlank()) {
				UiState.Error("No server saved")
			}
			else {
				UiState.Success(it)
			}
		}
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			UiState.Init
		)

	val isRealTimeOn = settingsRepository.isRealTimeOn
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			false
		)

	private val _dialogIsSelfHosted = MutableStateFlow(false)
	fun setDialogIsSelfHosted(isSelfHosted: Boolean) {
		_dialogIsSelfHosted.update { isSelfHosted }
	}
	fun toggleDialogIsSelfHosted() {
		_dialogIsSelfHosted.update { !_dialogIsSelfHosted.value }
	}

	fun getDialogIsSelfHostedText(): String {
		return if (_dialogIsSelfHosted.value) "Self-Hosted"
		else "Web"
	}

	private val _dialogInput = MutableStateFlow("")
	fun setDialogInput(str: String) {
		_dialogInput.update { str }
	}

	fun submitDialogFields() {
		viewModelScope.launch {
			//We are storing the flow values inside other variables in case some change happens
			// meanwhile
			val serverChoice = ServerChoice(_dialogInput.value, _dialogIsSelfHosted.value)
			when(val res = checkIsBus2GoServer.invoke(serverChoice.server, serverChoice.isSelfHosted.toServerType())){
				is Result.Error -> {
					//FIXME do we really store something nothing?
					settingsRepository.setBus2GoServer(ServerChoice("", true))
					val cause = res.throwable?.findCause<UnpinnedCertificateException>()
					if (cause == null) {
						res.message?.also { _toastText.emit(Response(false, it, null)) }
							?: _toastText.emit(Response(false, "Some unknown error occurred", null))
					}
					else {
						_toastText.emit(Response(false, "SSL/TLS certificate error", null))
					}
				}
				is Result.Success<Boolean> -> {
					if (res.data){
						settingsRepository.setBus2GoServer(serverChoice)
						_toastText.emit(Response(true, "Server Changed Successfully", serverChoice.server))
					}
					else {
						settingsRepository.setBus2GoServer(ServerChoice("", true))
						_toastText.emit(Response(false, "Invalid Bus2Go Server", null))
					}
				}
			}
		}
	}

	private fun Boolean.toServerType(): ServerType {
		return if (this) ServerType.SELF_HOSTED
		else ServerType.WEB
	}

}