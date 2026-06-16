package dev.mainhq.bus2go.presentation.main.home.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime


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
                .filter { tag == null || it.favouriteTransitData.tags.map { it.label }.contains(tag) }
                .map {
                    if (it.arrivalTime != null) {
                        val timeRemaining = it.arrivalTime.timeRemaining()
                        val isUrgent =
                            if (timeRemaining == null || timeRemaining < LocalTime.of(0, 4, 0))
                                Urgency.IMMINENT
                            else if (timeRemaining < LocalTime.of(0, 11, 0))
                                Urgency.SOON
                            else
                                Urgency.DISTANT
                        when (it.favouriteTransitData) {
                            is ExoBusFavouriteItem -> {
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

                            is ExoTrainFavouriteItem -> {
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

                            is StmBusFavouriteItem -> {
                                FavouritesDisplayModel(
                                    favouriteTransitData = it.favouriteTransitData,
                                    directionText = "To ${it.favouriteTransitData.lastStop}",
                                    toTruncate = it.favouriteTransitData.lastStop.length > FavouritesDisplayModel.DIRECTION_STR_LIMIT,
                                    tripHeadsignText = it.favouriteTransitData.routeId,
                                    stopNameText = it.favouriteTransitData.stopName,
                                    arrivalTimeText = it.arrivalTime.getTimeString(),
                                    timeRemainingText = getTimeRemaining(timeRemaining),
                                    dataDisplayColor = R.color.basic_blue,
                                    urgency = isUrgent
                                )
                            }
                        }
                    }
                    else {
                        when (it.favouriteTransitData) {
                            is ExoBusFavouriteItem -> {
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

                            is ExoTrainFavouriteItem -> {
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

                            is StmBusFavouriteItem -> {
                                FavouritesDisplayModel(
                                    favouriteTransitData = it.favouriteTransitData,
                                    directionText = it.favouriteTransitData.lastStop,
                                    toTruncate = it.favouriteTransitData.lastStop.length
                                            > FavouritesDisplayModel.DIRECTION_STR_LIMIT,
                                    tripHeadsignText = it.favouriteTransitData.routeId,
                                    stopNameText = it.favouriteTransitData.stopName,
                                    arrivalTimeText = null,
                                    timeRemainingText = "None left",
                                    dataDisplayColor = R.color.basic_blue,
                                    urgency = Urgency.DISTANT
                                )
                            }
                        }
                    }
                }
        )}.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = UiState.Loading)

    private val _favouritesToRemove: MutableStateFlow<List<FavouriteTransitData>> = MutableStateFlow(listOf())
    val favouritesToRemove = _favouritesToRemove.asStateFlow()

    //changes between selection mode for removing favourites and shit, or normal mode where we can click
    private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()
    private val _wasSelectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)


    fun selectTag(tag: String){
        _selectedTag.update { tag }
    }

    fun unselectTag() {
        _selectedTag.update { null }
    }

    private fun getTimeRemaining(remainingTime: LocalTime?): String {
        return if (remainingTime != null && remainingTime.hour > 0) {
            if (remainingTime.minute > 9) "In ${remainingTime.hour}h${remainingTime.minute}"
             else "In ${remainingTime.hour}h0${remainingTime.minute}"
        }
        else if (remainingTime != null) "In ${remainingTime.minute} min"
        else "Bus has passed??"
    }

    fun activateSelectionMode(){
        _selectionMode.update { true }
    }

    fun deactivateSelectionMode(){
        deselectAllForRemoval()
        _selectionMode.update { false }
    }

    //FIXME needs an argument to know which favourite we selected
    fun toggleFavouriteForRemoval(favouriteTransitData: FavouriteTransitData){
        //remove from removal
        if (_favouritesToRemove.value.contains(favouriteTransitData)){
            _favouritesToRemove.update { curToRemoveList ->
                val list = curToRemoveList.toMutableList()
                list.remove(favouriteTransitData)
                list
            }
        }
        //add for removal
        else {
            _favouritesToRemove.update { curToRemoveList ->
                val list = curToRemoveList.toMutableList()
                list.add(favouriteTransitData)
                list
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

    fun deselectAllForRemoval(){
        _favouritesToRemove.update{
            val list = it.toMutableList()
            list.clear()
            list
        }
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