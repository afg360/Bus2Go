package dev.mainhq.bus2go.presentation.stop_direction.stop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.compareTransitData
import dev.mainhq.bus2go.domain.use_case.favourites.AddFavourite
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavourites
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StopFragmentViewModel(
	private val addFavourite: AddFavourite,
	private val removeFavourite: RemoveFavourite,
	getFavourites: GetFavourites
): ViewModel() {


	private val _stopNames: MutableStateFlow<List<TransitData>> = MutableStateFlow(listOf())
	val stopNames = _stopNames.asStateFlow()

	val favourites = getFavourites.invoke()
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), hashMapOf())

	fun setTransitData(transitData: List<TransitData>){
		viewModelScope.launch {
			_stopNames.update { transitData }
		}
	}

	fun addFavourite(data : TransitData){
		viewModelScope.launch {
			addFavourite.invoke(data)
		}
	}

	fun removeFavourite(data : TransitData){
		viewModelScope.launch {
			removeFavourite.invoke(data)
		}
	}
}