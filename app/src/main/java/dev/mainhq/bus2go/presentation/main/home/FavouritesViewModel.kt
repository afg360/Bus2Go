package dev.mainhq.bus2go.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.use_case.favourites.AddTag
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavouritesWithTimeData
import dev.mainhq.bus2go.domain.use_case.favourites.MoveFavourite
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import kotlin.time.toDuration

class FavouritesViewModel(
	getFavouritesWithTimeData: GetFavouritesWithTimeData,
	private val removeFavourite: RemoveFavourite,
	private val moveFavourite: MoveFavourite,
	private val addTag: AddTag
) : ViewModel(){

    //3) eventually some sort of sorting/categorisation of favourites
    //The whole data set to be displayed initially
    //What is actually displayed on the screen

    //should be using stateFlow, but I hate that goes through an empty list first and that gets displayed...
	private val _favouriteTransitData = getFavouritesWithTimeData.invoke().map { favouritesTimeWithData ->
                when(favouritesTimeWithData) {
                    is Result.Error -> { emptyList() }
                    is Result.Success<List<FavouriteTransitDataWithTime>> -> {
						favouritesTimeWithData.data.ifEmpty { emptyList() }
                    }
        }
    }

    //null if none selected
    private val _selectedTag: MutableStateFlow<String?> = MutableStateFlow(null)

    val favouriteDisplayTransitData = combine(
		_favouriteTransitData,
		_selectedTag
	) { favourites, tag ->
		UiState.Success(
			data = favourites
				.filter {
					tag == null || it.favouriteTransitData.tags.map { it.label }.contains(tag)
				}
				.map {
					if (it.arrivalTime != null) {
						val timeRemaining = it.arrivalTime.timeRemaining()
						val isUrgent =
							if (timeRemaining == null || timeRemaining < Duration.ofMinutes(4))
								Urgency.IMMINENT
							else if (timeRemaining < Duration.ofMinutes(11))
								Urgency.SOON
							else
								Urgency.DISTANT
						when (it.favouriteTransitData) {
							is FavouriteTransitData.ExoBusFavouriteItem -> {
								FavouritesDisplayModel(
									favouriteTransitData = it.favouriteTransitData,
									directionText = "To ${it.favouriteTransitData.direction}",
									toTruncate = it.favouriteTransitData.direction.length > FavouritesDisplayModel.DIRECTION_STR_LIMIT,
									tripHeadsignText = it.favouriteTransitData.routeId,
									stopNameText = it.favouriteTransitData.stopName,
									arrivalTimeText = it.arrivalTime.getTimeString(),
									timeRemainingText = getTimeRemaining(timeRemaining),
									dataDisplayColor = R.color.basic_purple,
									urgency = isUrgent
								)
							}

							is FavouriteTransitData.ExoTrainFavouriteItem -> {
								FavouritesDisplayModel(
									favouriteTransitData = it.favouriteTransitData,
									directionText = "To ${it.favouriteTransitData.direction}",
									toTruncate = it.favouriteTransitData.direction.length > FavouritesDisplayModel.DIRECTION_STR_LIMIT,
									tripHeadsignText = it.favouriteTransitData.routeName,
									stopNameText = it.favouriteTransitData.stopName,
									arrivalTimeText = it.arrivalTime.getTimeString(),
									timeRemainingText = getTimeRemaining(timeRemaining),
									dataDisplayColor = R.color.orange,
									urgency = isUrgent
								)
							}

							is FavouriteTransitData.StmBusFavouriteItem -> {
								FavouritesDisplayModel(
									favouriteTransitData = it.favouriteTransitData,
									directionText = "To ${it.favouriteTransitData.lastStop}",
									toTruncate = it.favouriteTransitData.lastStop.length > FavouritesDisplayModel.DIRECTION_STR_LIMIT,
									tripHeadsignText = it.favouriteTransitData.routeId,
									stopNameText = it.favouriteTransitData.stopName,
									arrivalTimeText = it.arrivalTime.getTimeString(),
									timeRemainingText = getTimeRemaining(timeRemaining),
									dataDisplayColor = if (it.favouriteTransitData.routeId.toInt() in 400..499) {
										R.color.basic_green
									} else {
										R.color.basic_blue
									},
									urgency = isUrgent
								)
							}
						}
					} else {
						when (it.favouriteTransitData) {
							is FavouriteTransitData.ExoBusFavouriteItem -> {
								FavouritesDisplayModel(
									favouriteTransitData = it.favouriteTransitData,
									directionText = it.favouriteTransitData.direction,
									toTruncate = it.favouriteTransitData.direction.length > FavouritesDisplayModel.DIRECTION_STR_LIMIT,
									tripHeadsignText = it.favouriteTransitData.routeId,
									stopNameText = it.favouriteTransitData.stopName,
									arrivalTimeText = null,
									timeRemainingText = "None left",
									dataDisplayColor = R.color.basic_purple,
									urgency = Urgency.DISTANT
								)
							}

							is FavouriteTransitData.ExoTrainFavouriteItem -> {
								FavouritesDisplayModel(
									favouriteTransitData = it.favouriteTransitData,
									directionText = "To ${it.favouriteTransitData.direction}",
									toTruncate = it.favouriteTransitData.direction.length
											> FavouritesDisplayModel.DIRECTION_STR_LIMIT,
									tripHeadsignText = it.favouriteTransitData.routeName,
									stopNameText = it.favouriteTransitData.stopName,
									arrivalTimeText = null,
									//FIXME use a ressource string that is injected into the class...
									timeRemainingText = "None left",
									dataDisplayColor = R.color.orange,
									urgency = Urgency.DISTANT
								)
							}

							is FavouriteTransitData.StmBusFavouriteItem -> {
								FavouritesDisplayModel(
									favouriteTransitData = it.favouriteTransitData,
									directionText = it.favouriteTransitData.lastStop,
									toTruncate = it.favouriteTransitData.lastStop.length
											> FavouritesDisplayModel.DIRECTION_STR_LIMIT,
									tripHeadsignText = it.favouriteTransitData.routeId,
									stopNameText = it.favouriteTransitData.stopName,
									arrivalTimeText = null,
									timeRemainingText = "None left",
									dataDisplayColor = if (it.favouriteTransitData.routeId.toInt() in 400..499) {
										R.color.basic_green
									} else {
										R.color.basic_blue
									},
									urgency = Urgency.DISTANT
								)
							}
						}
					}
				}
		)
	}.stateIn(viewModelScope, started = SharingStarted.Companion.WhileSubscribed(5000), initialValue = UiState.Loading)

    fun selectTag(tag: String){
        _selectedTag.update { tag }
    }

    fun unselectTag() {
        _selectedTag.update { null }
    }

    private fun getTimeRemaining(remainingTime: Duration?): String {
        return if (remainingTime != null && remainingTime.toHours() > 0) {
            if (remainingTime > Duration.ofMinutes(9)) "In ${remainingTime.toHours()}h${remainingTime.toMinutes() % 60}"
             else "In ${remainingTime.toHours()}h0${remainingTime.toMinutes() % 60}"
        }
        else if (remainingTime != null) "In ${remainingTime.toMinutes() % 60} min"
        else "Bus has passed??"
    }

    fun activateSelectionMode(){
        _selectionMode.update { true }
    }

    fun deactivateSelectionMode(){
        deselectAllForRemoval()
        _selectionMode.update { false }
    }

    private val _favouritesToRemove: MutableStateFlow<List<FavouriteTransitData>> =
		MutableStateFlow(listOf())
    val favouritesToRemove = _favouritesToRemove.asStateFlow()

    //changes between selection mode for removing favourites and shit, or normal mode where we can click
    private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()

    //we are using a nullable boolean to be able to use a "3-valued based flag", null being
    //"in between" true and false
    val selectAllFavourites = combine(
		_favouriteTransitData,
		favouritesToRemove
	) { favouritesTransitData, favouritesToRemove ->
		if (_favouritesToRemove.value.isEmpty()) {
			false
		} else if (favouritesTransitData.size != favouritesToRemove.size) {
			null
		} else {
			true
		}
	}.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), null)

    fun toggleFavouriteForRemoval(favouriteTransitData: FavouriteTransitData){
        //remove from removal
        if (_favouritesToRemove.value.contains(favouriteTransitData)){
            _favouritesToRemove.update { curToRemoveList ->
                curToRemoveList - favouriteTransitData
            }
        }
        //add for removal
        else {
            _favouritesToRemove.update { curToRemoveList ->
                 curToRemoveList + favouriteTransitData
            }
        }
    }

    /**
     * The selection occurs on what is displayed. So if we've selected a tag, selects only everything
     * containing the tag
     */
    fun selectAllForRemoval(){
        _favouritesToRemove.update {
            when(val state = favouriteDisplayTransitData.value){
                is UiState.Success<List<FavouritesDisplayModel>> -> {
                    state.data.map { it.favouriteTransitData }
                }
                else -> throw IllegalStateException("Expected to be in the Success State")
            }
        }
    }

    /** Only toggles the all checkbox, doesn't deselect anything else */
    fun toggleSelectAllFavourites() {
        viewModelScope.launch {
            if (_favouritesToRemove.value.size < _favouriteTransitData.first().size) {
                _favouritesToRemove.update {
                    _favouriteTransitData.first().map { it.favouriteTransitData }
                }
            }
            //Must assert that maximum equal to the same size
            else {
                _favouritesToRemove.update { emptyList() }
            }
        }
    }

    fun deselectAllForRemoval(){
        _favouritesToRemove.update{ emptyList() }
    }

    //TODO when removing favourites, we also may need to update tags
    fun removeFavourites(){
        viewModelScope.launch {
            _favouritesToRemove.value
                .map { favouriteTransitData ->
                    async { removeFavourite.invoke(favouriteTransitData) }
                }.awaitAll()

            if (_favouritesToRemove.value.isEmpty()){
                _selectionMode.update { false }
            }
            _favouritesToRemove.update { emptyList() }
        }
    }

    fun moveFavourite(oldPosition: Int, newPosition: Int) {
        viewModelScope.launch {
            moveFavourite.invoke(oldPosition, newPosition)
        }
    }

    fun addTag(tag: String, transitData: List<FavouriteTransitData>){
        viewModelScope.launch {
            //TODO choose a random color
            val tagToAdd = Tag(tag, 0xFFFFFF)
            addTag.invoke(tagToAdd, transitData)
        }
    }
}