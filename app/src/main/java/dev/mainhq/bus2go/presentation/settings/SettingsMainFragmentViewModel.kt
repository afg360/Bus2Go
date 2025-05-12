package dev.mainhq.bus2go.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.use_case.CheckIsBus2GoServer
import dev.mainhq.bus2go.domain.use_case.SaveBus2GoServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsMainFragmentViewModel(
	private val checkIsBus2GoServer: CheckIsBus2GoServer,
	private val saveBus2GoServer: SaveBus2GoServer
): ViewModel() {

	private val _toastText = MutableSharedFlow<Response>(replay = 0)
	val toastText = _toastText.asSharedFlow()

	fun checkIsBus2GoServer(string: String) {
		viewModelScope.launch(Dispatchers.IO) {
			//TODO perhaps isntead of replacing with empty, dont save anything (keep the older one)
			when(val res = checkIsBus2GoServer.invoke(string)){
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
}