package dev.mainhq.bus2go.presentation.main.home.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.use_case.favourites.GetAllTags
import dev.mainhq.bus2go.presentation.main.home.TagEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.invoke

/** ViewModel shared between FavouritesFragment and HomeFragment (its parent) */
class FavouritesFragmentSharedViewModel(
	private val getAllTags: GetAllTags,
): ViewModel() {

	private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val selectionMode = _selectionMode.asStateFlow()

	//we are using a nullable boolean to be able to use a "3-valued based flag", null being
	//"in between" true and false
	private val _selectAllFavourites: MutableStateFlow<Boolean?> = MutableStateFlow(false)
	val selectAllFavourites = _selectAllFavourites.asStateFlow()

	//TODO need to share how many to remove and change the garbage button or not
	private val _numberFavouritesSelected: MutableStateFlow<Int> = MutableStateFlow(0)
	val numberFavouritesSelected = _numberFavouritesSelected.asStateFlow()

	private val _tags: MutableStateFlow<List<Tag>> = MutableStateFlow(emptyList())
	val tags = _tags.asStateFlow()

	init {
		viewModelScope.launch {
			_tags.update { getAllTags.invoke() }
		}
	}

	fun activateSelectionMode(){
		_selectionMode.update { true }
	}

	fun deactivateSelectionMode(){
		_selectionMode.update { false }
		_numberFavouritesSelected.update { 0 }
	}

	private val _tagSelected: MutableStateFlow<String?> = MutableStateFlow(null)
	val tagSelected = _tagSelected.asStateFlow()

	private val _tagEvent: MutableSharedFlow<TagEvent> = MutableSharedFlow()
	val tagEvent = _tagEvent.asSharedFlow()

	/**
	 * Filter in only favourites containing the input tag.
	 * If the tag is already selected, toggle it off
	 * */
	fun triggerFilterTagToFavouritesEvent(tag: String) {
		viewModelScope.launch {
			if (tag.isEmpty() && _tagSelected.value != null){
				_tagEvent.emit(TagEvent.RemoveTagFilter)
				_tagSelected.update { null }
			}
			else {
				if (tag != _tagSelected.value) {
					_tagEvent.emit(TagEvent.FilterFavouritesWithTagEvent(tag))
					_tagSelected.update { tag }
				}
				else {
					_tagEvent.emit(TagEvent.RemoveTagFilter)
					_tagSelected.update { null }
				}
			}
		}
	}

	/**
	 * Trigger adding a tag to a favourite. If the tag doesn't exist, also add it to the list of tags
	 * */
	fun triggerAddTagToFavouritesEvent(newTag: String) {
		if (!tags.value.map { it.label }.contains(newTag)){
			viewModelScope.launch {
				//TODO choose a random color
				val tagToAdd = Tag(newTag, 0xFFFFFF)
				_tags.update { _tags.value.toMutableList().apply { add(tagToAdd) } }
				_tagEvent.emit(TagEvent.AddTagEvent(newTag))
			}
		}
	}

	fun triggerRemoveTagToFavouritesEvent(newTag: String) {
		if (!tags.value.map { it.label }.contains(newTag)){
			viewModelScope.launch {
				val tagToAdd = Tag(newTag, 0xFFFFFF)
				_tags.update { _tags.value.toMutableList().apply { add(tagToAdd) } }
				_tagEvent.emit(TagEvent.AddTagEvent(newTag))
			}
		}
	}

	/** Only toggles the all checkbox, doesn't deselect anything else */
	fun toggleSelectAllFavourites(){
		_selectAllFavourites.update { _selectAllFavourites.value?.not() ?: true }
	}

	fun toggleIsAllSelected(checked: Boolean){
		when (_selectAllFavourites.value) {
			true -> _selectAllFavourites.update { null }
			false -> _selectAllFavourites.update { null }
			else -> {
				_selectAllFavourites.update { if (checked) true else null }
			}
		}
	}

	fun incrementNumFavouritesSelected(){
		_numberFavouritesSelected.update { it + 1 }
	}

	fun decrementNumFavouritesSelected(){
		_numberFavouritesSelected.update { it - 1 }
	}

	fun setAllFavouritesSelected(num: Int){
		_numberFavouritesSelected.update { num }
	}

	fun resetNumFavouritesSelected(){
		_numberFavouritesSelected.update { 0 }
	}

}