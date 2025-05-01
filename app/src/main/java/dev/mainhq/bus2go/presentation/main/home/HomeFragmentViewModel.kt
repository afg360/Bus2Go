package dev.mainhq.bus2go.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeFragmentViewModel(
	private val getRouteInfo: GetRouteInfo
): ViewModel() {

	private val _searchQuery: MutableStateFlow<List<RouteInfo>> = MutableStateFlow(listOf())
	val searchQuery = _searchQuery.asStateFlow()

	private val _isSearching: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isSearching = _isSearching.asStateFlow()

	//we set replay to 0 so that back button previously made are not executed
	//using a Unit bcz we are not storing data but rather the fact that we trigger an event
	private val _isBackPressed: MutableSharedFlow<Unit> = MutableSharedFlow(0)
	val isBackPressed = _isBackPressed.asSharedFlow()

	fun onSearchQueryChange(query: String){
		if (query.isEmpty()){
			_searchQuery.value = listOf()
		}
		else{
			viewModelScope.launch {
				when(val routeInfo = getRouteInfo.invoke(query)){
					is Result.Error -> {
						TODO()
					}
					is Result.Success<List<RouteInfo>> -> {
						_searchQuery.value = routeInfo.data
					}
				}
			}
		}
	}

	suspend fun triggerBackPressed(){
		//sends a signal to collectors to consume the event (or do something about it)
		_isBackPressed.emit(Unit)
	}
}