package dev.mainhq.bus2go.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ServerChoice
import dev.mainhq.bus2go.domain.repository.SettingsRepository
import dev.mainhq.bus2go.domain.use_case.settings.CheckIsBus2GoServer
import dev.mainhq.bus2go.domain.use_case.settings.SaveBus2GoServer
import dev.mainhq.bus2go.presentation.config.ServerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsMainFragmentViewModel(
	private val settingsRepository: SettingsRepository,
	private val checkIsBus2GoServer: CheckIsBus2GoServer,
	private val saveBus2GoServer: SaveBus2GoServer
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
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			ServerChoice(
				"",
				true
			)
		)

	//FIXME logic for ServerType
	fun checkIsBus2GoServer(string: String) {
		viewModelScope.launch {
			//TODO perhaps isntead of replacing with empty, dont save anything (keep the older one)
			when(val res = checkIsBus2GoServer.invoke(string, ServerType.SELF_HOSTED)){
				is Result.Error -> {
					saveBus2GoServer.invoke("")
					res.message?.also { _toastText.emit(Response(false, it, null)) }
						?: _toastText.emit(Response(false, "Some unknown error occurred", null))
				}
				is Result.Success<Boolean> -> {
					withContext(Dispatchers.Main){
						if (res.data){
							saveBus2GoServer.invoke(string)
							_toastText.emit(Response(true, "Server Changed Successfully", string))
						}
						else {
							saveBus2GoServer.invoke("")
							_toastText.emit(Response(false, "Invalid Bus2Go Server", null))
						}
					}
				}
			}
		}
	}

	val isRealTimeOn = settingsRepository.isRealTimeOn
		.stateIn(
			viewModelScope,
			SharingStarted.WhileSubscribed(5000),
			false
		)

}