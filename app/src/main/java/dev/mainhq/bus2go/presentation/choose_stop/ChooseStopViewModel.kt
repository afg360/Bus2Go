package dev.mainhq.bus2go.presentation.choose_stop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.TransitData
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChooseStopViewModel: ViewModel() {



	//TODO what if we already have the same data...?
	fun addFavourite(data : TransitData){
		viewModelScope.launch {
			favouritesUseCases.addFavourite(data)
			_favouriteTransitData.update {
				val list = it.toMutableList()
				list.add(data)
				return@update list
			}
		}
	}

}