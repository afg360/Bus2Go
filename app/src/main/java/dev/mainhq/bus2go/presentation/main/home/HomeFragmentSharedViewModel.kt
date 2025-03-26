package dev.mainhq.bus2go.presentation.main.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * ViewModel shared between HomeFragment and MainActivity
 **/
class HomeFragmentSharedViewModel: ViewModel() {

	//we set replay to 0 so that back button previously made are not executed
	//using a Unit bcz we are not storing data but rather the fact that we trigger an event
	private val _isBackPressed: MutableSharedFlow<Unit> = MutableSharedFlow(0)
	val isBackPressed = _isBackPressed.asSharedFlow()

	suspend fun triggerBackPressed(){
		//sends a signal to collectors to consume the event (or do something about it)
		_isBackPressed.emit(Unit)
	}

}