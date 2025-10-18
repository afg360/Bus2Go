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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StopFragmentViewModel(
	private val addFavourite: AddFavourite,
	private val removeFavourite: RemoveFavourite,
	private val getFavourites: GetFavourites
): ViewModel() {


	private val _stopNames: MutableStateFlow<List<TransitData>> = MutableStateFlow(listOf())
	val stopNames = _stopNames.asStateFlow()

	private val _favourites: MutableStateFlow<List<TransitData>> = MutableStateFlow(listOf())
	val favourites = _favourites.asStateFlow()

	init {
		viewModelScope.launch {
			_favourites.update { getFavourites.invoke() }
		}
	}

	fun setTransitData(transitData: List<TransitData>){
		viewModelScope.launch {
			_stopNames.update { transitData }
			_favourites.update { getFavourites.invoke().filter { transitData.compareTransitData(it) } }
		}
	}

	fun addFavourite(data : TransitData){
		_favourites.value.also { favourites ->
			if (!favourites.compareTransitData(data)){
				viewModelScope.launch {
					addFavourite.invoke(data)
					_favourites.update {
						val list = it.toMutableList()
						list.add(data)
						list
					}
				}
			}
			else {
				throw IllegalStateException("You cannot call addFavourite when you already added the same favourite (you must toggle between adding and removing)")
			}
		}
	}

	fun removeFavourite(data : TransitData){
		favourites.value.also { favourites ->
			if (favourites.compareTransitData(data)){
				viewModelScope.launch {
					removeFavourite.invoke(data)
					_favourites.update {
						val list = it.toMutableList()
						list.remove(data)
						list
					}
					//TODO
				}
			}
			else {
				throw IllegalStateException("You cannot call removeFavourite when you never had this favourite (you must toggle between adding and removing)")
			}
		}
	}
}