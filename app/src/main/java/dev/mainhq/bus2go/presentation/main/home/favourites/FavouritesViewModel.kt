package dev.mainhq.bus2go.presentation.main.home.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.use_case.FavouritesUseCases
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class FavouritesViewModel(
    private val favouritesUseCases: FavouritesUseCases
) : ViewModel(){

    //TODO
    //1) test the whole thing
    //2) add observer to check for click events
    //3) eventually some sort of sorting/categorisation of favourites

    /**
     * A helper class that let's us know if a favouriteTransitDataWithTime is selected
     **/
    private data class FavouriteTransitDataWithTimeAndSelection(
        val favouriteTransitDataWithTime: FavouriteTransitDataWithTime,
        var isSelected: Boolean
    )

    private val _favouriteTransitData: MutableStateFlow<List<FavouriteTransitDataWithTimeAndSelection>> = MutableStateFlow(listOf())
    val favouriteTransitData get() = _favouriteTransitData
        .map { list -> list.map { it.favouriteTransitDataWithTime } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            listOf()
        )

    //changes between selection mode for removing favourites and shit, or normal mode where we can click
    private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()


    init {
        //every 5 refresh the time displayed
        viewModelScope.launch {
            while (true){
                //FIXME this code always resets the isSelected to false...
                //FIXME code seems inneficient by going so many times to the repo... perhaps only
                //do it when some time is less than some other time
                _favouriteTransitData.value = favouritesUseCases.getFavouritesWithTimeData()
                    .map{ FavouriteTransitDataWithTimeAndSelection(it, false) }
                delay(5000)
            }
        }
    }

    fun activateSelectionMode(){
        _selectionMode.value = true
    }

    fun deactivateSelectionMode(){
        cancelRemoval()
        _selectionMode.value = false
    }

    //FIXME needs an argument to know which favourite we selected
    fun selectFavouriteForRemoval(index: Int){
        _favouriteTransitData.update { curList ->
            curList[index].isSelected = true
            curList
        }
    }

    fun deselectFavouriteForRemoval(index: Int){
        _favouriteTransitData.update { curList ->
            curList[index].isSelected = false
            curList
        }
    }

    fun selectAllForRemoval(){
        _favouriteTransitData.update { list ->
            list.map { FavouriteTransitDataWithTimeAndSelection(
                it.favouriteTransitDataWithTime,
                true
            )}
        }
    }

    fun deselectAllForRemoval(){
        _favouriteTransitData.update { list ->
            list.map { FavouriteTransitDataWithTimeAndSelection(
                it.favouriteTransitDataWithTime,
                false
            )}
        }
    }

    private fun cancelRemoval(){
        //FIXME isnt even necessary to call this?
        _favouriteTransitData.update { list ->
            list.map { FavouriteTransitDataWithTimeAndSelection(
                it.favouriteTransitDataWithTime,
                false
            )}
        }
    }

    fun removeFavourites(){
        viewModelScope.launch {
            val jobs = _favouriteTransitData.value
                .filter { it.isSelected }
                .map { favouriteTransitData ->
                    async {
                        favouritesUseCases.removeFavourite(favouriteTransitData.favouriteTransitDataWithTime.favouriteTransitData)
                        _favouriteTransitData.update {
                            val list = it.toMutableList()
                            list.removeIf { data ->
                                data.favouriteTransitDataWithTime.favouriteTransitData ==
                                        favouriteTransitData.favouriteTransitDataWithTime
                                            .favouriteTransitData
                            }
                            return@update list
                        }
                    }
                }
            jobs.forEach { it.await() }
        }
    }


    override fun onCleared() {
        //in case we need to reset the mutableStateFlows
        super.onCleared()
    }
}