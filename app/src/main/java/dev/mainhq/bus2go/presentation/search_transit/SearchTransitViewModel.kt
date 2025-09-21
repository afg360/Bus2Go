package dev.mainhq.bus2go.presentation.search_transit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchTransitViewModel(
	private val getRouteInfo: GetRouteInfo
): ViewModel() {

	private val _routeInfo: MutableStateFlow<UiState<List<RouteInfo>>> = MutableStateFlow(UiState.Success(listOf()))
	val routeInfo = _routeInfo.asStateFlow()

	fun queryRouteInfo(query: String){
		viewModelScope.launch {
			val routeInfo = getRouteInfo.invoke(query)
			_routeInfo.update {
				when(routeInfo){
					is Result.Error -> {
						UiState.Error("No db ma man")
					}
					is Result.Success<List<RouteInfo>> -> {
						UiState.Success(routeInfo.data)
					}
				}
			}
		}
	}

}