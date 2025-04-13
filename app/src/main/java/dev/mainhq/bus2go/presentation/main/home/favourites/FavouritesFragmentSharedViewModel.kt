package dev.mainhq.bus2go.presentation.main.home.favourites

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** ViewModel shared between FavouritesFragment and HomeFragment (its parent) */
class FavouritesFragmentSharedViewModel: ViewModel() {

	private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val selectionMode = _selectionMode.asStateFlow()

	//we are using a nullable boolean to be able to use a "3-valued based flag", null being
	//"in between" true and false
	private val _selectAllFavourites: MutableStateFlow<Boolean?> = MutableStateFlow(false)
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
		_selectAllFavourites.value = _selectAllFavourites.value?.not() ?: true
	}

	fun toggleIsAllSelected(checked: Boolean){
		when (_selectAllFavourites.value) {
			true -> _selectAllFavourites.value = null
			false -> _selectAllFavourites.value = null
			else -> {
				_selectAllFavourites.value = if (checked) true else null
			}
		}
	}

	fun incrementNumFavouritesSelected(){
		_numberFavouritesSelected.value++
	}

	fun decrementNumFavouritesSelected(){
		_numberFavouritesSelected.value--
	}

	fun setAllFavouritesSelected(num: Int){
		_numberFavouritesSelected.value = num
	}

	fun resetNumFavouritesSelected(){
		_numberFavouritesSelected.value = 0
	}

}