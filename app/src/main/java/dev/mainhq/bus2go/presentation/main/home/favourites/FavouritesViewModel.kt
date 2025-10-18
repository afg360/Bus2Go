package dev.mainhq.bus2go.presentation.main.home.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.use_case.favourites.AddTag
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavouritesWithTimeData
import dev.mainhq.bus2go.domain.use_case.favourites.GetAllTags
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime


class FavouritesViewModel(
    private val getFavouritesWithTimeData: GetFavouritesWithTimeData,
    private val removeFavourite: RemoveFavourite,
    private val addTag: AddTag
) : ViewModel(){

    private val _running = MutableStateFlow(true)
    //3) eventually some sort of sorting/categorisation of favourites
    //The whole data set to be displayed initially
    //What is actually displayed on the screen
    private val _favouriteTransitData = flow {
        while (_running.value) {
            //FIXME code seems inefficient by going so many times to the repo... perhaps only
            // useless when no favourites made...
            // do it when some time is less than some other time
            when (val favouritesWithTimeData = getFavouritesWithTimeData.invoke()) {
                is Result.Error -> TODO()
                is Result.Success<List<TransitDataWithTime>> -> {
                    if (favouritesWithTimeData.data.isNotEmpty()) {
                        emit(favouritesWithTimeData.data)
                    }
                    else {
                        emit(emptyList())
                        //_running.update { false }
                    }
                }
            }
            delay(1000)
        }
    }.shareIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), replay = 1)

    //null if none selected
    private val _selectedTag: MutableStateFlow<String?> = MutableStateFlow(null)

    //could also use a combine flow operation...?
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
                            if (timeRemaining == null || timeRemaining < LocalTime.of(
                                    0,
                                    4,
                                    0
                                )
                            ) Urgency.IMMINENT
                            else if (timeRemaining < LocalTime.of(
                                    0,
                                    11,
                                    0
                                )
                            ) Urgency.SOON
                            else Urgency.DISTANT
                        when (it.favouriteTransitData) {
                            is ExoBusItem -> {
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

                            is ExoTrainItem -> {
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

                            is StmBusItem -> {
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
                            is ExoBusItem -> {
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

                            is ExoTrainItem -> {
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

                            is StmBusItem -> {
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
        )}
        .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = UiState.Loading)

    private val _favouritesToRemove: MutableStateFlow<List<TransitData>> = MutableStateFlow(listOf())
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
    fun toggleFavouriteForRemoval(transitData: TransitData){
        //remove from removal
        if (_favouritesToRemove.value.contains(transitData)){
            _favouritesToRemove.update { curToRemoveList ->
                val list = curToRemoveList.toMutableList()
                list.remove(transitData)
                list
            }
        }
        //add for removal
        else {
            _favouritesToRemove.update { curToRemoveList ->
                val list = curToRemoveList.toMutableList()
                list.add(transitData)
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
                    async {
                        removeFavourite.invoke(favouriteTransitData)
                    }
                }.awaitAll()

            if (_favouritesToRemove.value.isEmpty()){
                _selectionMode.update { false }
            }
            _favouritesToRemove.update { emptyList() }
        }
    }

    fun addTag(tag: String, transitData: List<TransitData>){
        viewModelScope.launch {
            //TODO choose a random color
            val tagToAdd = Tag(tag, 0xFFFFFF)
            addTag.invoke(tagToAdd, transitData)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _running.update { false }
    }

}