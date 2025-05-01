package dev.mainhq.bus2go.presentation.main.home.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavouritesWithTimeData
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime


class FavouritesViewModel(
    private val getFavouritesWithTimeData: GetFavouritesWithTimeData,
    private val removeFavourite: RemoveFavourite
) : ViewModel(){

    //TODO
    //1) test the whole thing
    //2) add observer to check for click events
    //3) eventually some sort of sorting/categorisation of favourites

    //instead of init with an empty list, we have some flag to checks when the thing is ready

    private val _favouriteTransitData: MutableStateFlow<UiState<List<FavouritesDisplayModel>>> = MutableStateFlow(UiState.Loading)
    val favouriteTransitData get() = _favouriteTransitData.asStateFlow()

    private val _favouritesToRemove: MutableStateFlow<List<TransitData>> = MutableStateFlow(listOf())
    val favouritesToRemove get() = _favouritesToRemove.asStateFlow()

    //changes between selection mode for removing favourites and shit, or normal mode where we can click
    private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()


    //private val _wasSelectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        //refreshes the data every second
        //FIXME when the activity is onPause, stop generating data as it wastes battery life...
        viewModelScope.launch(Dispatchers.Default) {
            var running = true
            while (running){
                //FIXME code seems inefficient by going so many times to the repo... perhaps only
                // useless when no favourites made...
                //do it when some time is less than some other time
                _favouriteTransitData.value = UiState.Success(
                    when (val favouritesWithTimeData = getFavouritesWithTimeData.invoke()){
                        is Result.Error -> {
                            TODO()
                        }

                        is Result.Success<List<TransitDataWithTime>> -> {
                            favouritesWithTimeData.data.map{
                                if (it.arrivalTime != null) {
                                    val timeRemaining = it.arrivalTime.timeRemaining()
                                    val isUrgent = if (timeRemaining == null || timeRemaining < LocalTime.of(0, 4, 0))
                                        Urgency.IMMINENT
                                    else if (timeRemaining < LocalTime.of(0, 11, 0))
                                        Urgency.SOON
                                    else Urgency.DISTANT

                                    when(it.favouriteTransitData){
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
                                    when(it.favouriteTransitData){
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
                        }
                    }


                )
                if ((_favouriteTransitData.value as UiState.Success<List<FavouritesDisplayModel>>)
                        .data.isEmpty()) running = false
                delay(1000)
            }
        }
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
        _selectionMode.value = true
    }

    fun deactivateSelectionMode(){
        deselectAllForRemoval()
        _selectionMode.value = false
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

    fun selectAllForRemoval(){
        _favouritesToRemove.update {
            when(val state = favouriteTransitData.value){
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


    fun removeFavourites(){
        viewModelScope.launch {
            _favouritesToRemove.value
                .map { favouriteTransitData ->
                    async {
                        removeFavourite.invoke(favouriteTransitData)
                    }
                }.awaitAll()

            _favouriteTransitData.value = when (val state =_favouriteTransitData.value){
                is UiState.Success<List<FavouritesDisplayModel>> -> {
                    UiState.Success(state.data.filterNot { data ->
                        _favouritesToRemove.value.contains(data.favouriteTransitData)
                    })
                }
                else -> throw  IllegalStateException("Expected UI to be in the success state")
            }
            if (_favouritesToRemove.value.isEmpty()){
                _selectionMode.value = false
            }
            _favouritesToRemove.update { emptyList() }
        }
    }


    override fun onCleared() {
        //in case we need to reset the mutableStateFlows
        super.onCleared()
    }
}