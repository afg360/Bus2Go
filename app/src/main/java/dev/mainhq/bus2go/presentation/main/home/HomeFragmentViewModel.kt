package dev.mainhq.bus2go.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.use_case.favourites.GetAllTags
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeFragmentViewModel(
	private val getRouteInfo: GetRouteInfo,
): ViewModel() {

	private val _searchQuery: MutableStateFlow<UiState<List<RouteInfo>>> = MutableStateFlow(UiState.Success(listOf()))
	val searchQuery = _searchQuery.asStateFlow()

	private val _isSearching: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isSearching = _isSearching.asStateFlow()

	//we set replay to 0 so that back button previously made are not executed
	//using a Unit bcz we are not storing data but rather the fact that we trigger an event
	private val _isBackPressed: MutableSharedFlow<Unit> = MutableSharedFlow(0)
	val isBackPressed = _isBackPressed.asSharedFlow()

	fun onSearchQueryChange(query: String){
		if (query.isEmpty()){
			_searchQuery.update { UiState.Success(listOf()) }
		}
		else{
			viewModelScope.launch {
				val routeInfo = getRouteInfo.invoke(query)
				_searchQuery.update {
					when(routeInfo){
						is Result.Error -> {
							UiState.Error("no db ma man...")
						}
						is Result.Success<List<RouteInfo>> -> {
							UiState.Success(routeInfo.data)
						}
					}
				}
			}
		}
	}

	fun triggerBackPressed(){
		//sends a signal to collectors to consume the event (or do something about it)
		viewModelScope.launch {
			_isBackPressed.emit(Unit)
		}
	}
}