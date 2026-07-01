package dev.mainhq.bus2go.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.use_case.favourites.GetAllTags
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.invoke

class HomeFragmentViewModel(
	private val getRouteInfo: GetRouteInfo,
	private val getAllTags: GetAllTags,
): ViewModel() {

	private val _searchQuery: MutableStateFlow<UiState<List<RouteInfo>>> = MutableStateFlow(UiState.Success(listOf()))
	val searchQuery = _searchQuery.asStateFlow()

	private val _isSearching: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isSearching = _isSearching.asStateFlow()

	private val _isSelectionModeActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isSelectionModeActive = _isSelectionModeActive.asStateFlow()

	//we set replay to 0 so that back button previously made are not executed
	//using a Unit bcz we are not storing data but rather the fact that we trigger an event
	private val _isBackPressed: MutableSharedFlow<Unit> = MutableSharedFlow(0)
	val isBackPressed = _isBackPressed.asSharedFlow()

	fun onSearchQueryChange(query: String){
		if (query.isEmpty()){
			_searchQuery.update { UiState.Success(listOf()) }
		}
		else{
			viewModelScope.launch {
				val routeInfo = getRouteInfo.invoke(query)
				_searchQuery.update {
					when(routeInfo){
						is Result.Error -> {
							UiState.Error("no db ma man...")
						}
						is Result.Success<List<RouteInfo>> -> {
							UiState.Success(routeInfo.data)
						}
					}
				}
			}
		}
	}

	fun triggerBackPressed(){
		//sends a signal to collectors to consume the event (or do something about it)
		viewModelScope.launch {
			_isBackPressed.emit(Unit)
		}
	}

	fun activateSelectionMode(){
		_isSelectionModeActive.update { true }
	}

	fun deactivateSelectionMode(){
		_isSelectionModeActive.update { false }
	}


	//TODO transform into a cold flow .stateIn
	private val _tags: MutableStateFlow<List<Tag>> = MutableStateFlow(emptyList())
	val tags = _tags.asStateFlow()

	init {
		viewModelScope.launch {
			_tags.update { getAllTags.invoke() }
		}
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
}