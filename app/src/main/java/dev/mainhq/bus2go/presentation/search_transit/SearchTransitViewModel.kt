package dev.mainhq.bus2go.presentation.search_transit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchTransitViewModel(
	private val getRouteInfo: GetRouteInfo
): ViewModel() {

	private val _routeInfo: MutableStateFlow<List<RouteInfo>> = MutableStateFlow(listOf())
	val routeInfo = _routeInfo.asStateFlow()

	fun queryRouteInfo(query: String){
		viewModelScope.launch {
			when(val routeInfo = getRouteInfo.invoke(query)){
				is Result.Error -> {
					TODO()
				}
				is Result.Success<List<RouteInfo>> -> {
					_routeInfo.value = routeInfo.data
				}
			}
		}
	}

}