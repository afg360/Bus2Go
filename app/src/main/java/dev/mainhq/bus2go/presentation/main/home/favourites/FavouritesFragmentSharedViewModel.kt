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
		_selectionMode.value = true
		_numberFavouritesSelected.value = 0
	}

	fun toggleSelectAllFavourites(){
		_selectAllFavourites.value = !_selectAllFavourites.value
	}

	/** Should only be called by FavouritesFragment */
	fun setNumberFavouritesSelected(num: Int){
		_numberFavouritesSelected.value = num
	}

}