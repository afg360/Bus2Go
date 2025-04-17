package dev.mainhq.bus2go.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeFragmentViewModel(
	private val getRouteInfo: GetRouteInfo
): ViewModel() {

	private val _searchQuery: MutableStateFlow<List<RouteInfo>> = MutableStateFlow(listOf())
	val searchQuery = _searchQuery.asStateFlow()

	private val _isSearching: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isSearching = _isSearching.asStateFlow()

	fun onSearchQueryChange(query: String){
		if (query.isEmpty()){
			_searchQuery.value = listOf()
		}
		else{
			viewModelScope.launch {
				_searchQuery.value = getRouteInfo(query)
			}
		}
	}
}