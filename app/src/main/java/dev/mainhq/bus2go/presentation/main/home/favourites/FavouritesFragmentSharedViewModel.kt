package dev.mainhq.bus2go.presentation.main.home.favourites

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** ViewModel shared between FavouritesFragment and HomeFragment (its parent) */
class FavouritesFragmentSharedViewModel: ViewModel() {

	private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val selectionMode = _selectionMode.asStateFlow()

	private val _selectAllFavourites: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val selectAllFavourites = _selectAllFavourites.asStateFlow()

	//TODO need to share how many to remove and change the garbage button or not
	private val _numberFavouritesSelected: MutableStateFlow<Int> = MutableStateFlow(0)
	val numberFavouritesSelected = _numberFavouritesSelected.asStateFlow()

	fun activateSelectionMode(){
		_selectionMode.value = true
	}

	fun deactivateSelectionMode(){
		_selectionMode.value = false
		_numberFavouritesSelected.value = 0
	}

	/** Only toggles the all checkbox, doesn't deselect anything else */
	fun toggleSelectAllFavourites(){
		_selectAllFavourites.value = !_selectAllFavourites.value
	}

	fun toggleIsAllSelected(checked: Boolean){
		_selectAllFavourites.value = _selectAllFavourites.value && checked
	}

	fun incrementNumFavouritesSelected(){
		_numberFavouritesSelected.value++
	}

	fun decrementNumFavouritesSelected(){
		_numberFavouritesSelected.value--
	}

	fun resetNumFavouritesSelected(){
		_numberFavouritesSelected.value = 0
	}

}