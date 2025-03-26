package dev.mainhq.bus2go.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.use_case.TransitInfoUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeFragmentViewModel(
	private val transitInfoUseCases: TransitInfoUseCases
): ViewModel() {

	private val _searchQuery: MutableStateFlow<List<RouteInfo>> = MutableStateFlow(listOf())
	val searchQuery = _searchQuery.asStateFlow()

	fun onSearchQueryChange(query: String){
		if (query.isEmpty()){
			_searchQuery.value = listOf()
		}
		else{
			viewModelScope.launch {
				_searchQuery.value = transitInfoUseCases.getRouteInfo(query)
			}
		}
	}
}