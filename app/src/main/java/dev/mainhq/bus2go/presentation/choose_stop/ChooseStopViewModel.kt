package dev.mainhq.bus2go.presentation.choose_stop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.use_case.favourites.AddFavourite
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavourites
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChooseStopViewModel(
	transitData: List<TransitData>,
	private val addFavourite: AddFavourite,
	private val removeFavourite: RemoveFavourite,
	private val getFavourites: GetFavourites
): ViewModel() {

	private val _stopNames: MutableStateFlow<List<TransitData>> = MutableStateFlow(transitData)
	val stopNames = _stopNames.asStateFlow()

	private val _favourites: MutableStateFlow<List<TransitData>?> = MutableStateFlow(null)
	val favourites = _favourites.asStateFlow()

	init {
		viewModelScope.launch {
			_favourites.value = getFavourites().filter { transitData.contains(it) }
		}
	}


	fun addFavourite(data : TransitData){
		_favourites.value.also { favourites ->
			if (favourites?.contains(data) == false){
				viewModelScope.launch {
					addFavourite.invoke(data)
					_favourites.update {
						val list = it?.toMutableList()
						list?.add(data)
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
			if (favourites?.contains(data) == true){
				viewModelScope.launch {
					removeFavourite.invoke(data)
					_favourites.update {
						val list = it?.toMutableList()
						list?.remove(data)
						return@update list
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