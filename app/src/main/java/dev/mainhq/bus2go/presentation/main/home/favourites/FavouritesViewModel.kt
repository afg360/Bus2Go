package dev.mainhq.bus2go.presentation.main.home.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.use_case.FavouritesUseCases
import dev.mainhq.bus2go.presentation.main.alarms.AlarmCreationDialogBottomNavBar
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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


    private val _favouriteTransitData: MutableStateFlow<List<FavouriteTransitDataWithTimeAndSelection>> = MutableStateFlow(listOf())
    val favouriteTransitData get() = _favouriteTransitData.asStateFlow()

    //changes between selection mode for removing favourites and shit, or normal mode where we can click
    private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()

    private val _isAllSelected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAllSelected = _isAllSelected.asStateFlow()

    //private val _wasSelectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        //every 5 refresh the time displayed
        viewModelScope.launch {
            while (true){
                //FIXME this code always resets the isSelected to false...
                //FIXME code seems inneficient by going so many times to the repo... perhaps only
                //do it when some time is less than some other time
                val currentData = _favouriteTransitData.value
                val newData = favouritesUseCases.getFavouritesWithTimeData()
                    .map { newItem ->
                        // Find if this item existed in the previous list and was selected
                        //val existingItem = currentData.find { it.transitDataWithTime.favouriteTransitData.routeId == newItem.favouriteTransitData.routeId }
                        // Preserve selection state if item existed, otherwise default to false
                        val currentItem = currentData.find { it.transitDataWithTime.favouriteTransitData == newItem.favouriteTransitData }
                        FavouriteTransitDataWithTimeAndSelection(
                            transitDataWithTime = newItem,
                            isSelected = currentItem?.isSelected ?: false
                        )
                    }

                _favouriteTransitData.value = newData
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
    fun toggleFavouriteForRemoval(transitData: TransitData){
        _favouriteTransitData.update { curList ->
            val item = curList.find { it.transitDataWithTime.favouriteTransitData == transitData }
            item?.isSelected = item?.isSelected?.also { isSelected -> !isSelected } ?: false
            //curList[index].isSelected = true
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
                it.transitDataWithTime,
                true
            )}
        }
    }

    fun deselectAllForRemoval(){
        _favouriteTransitData.update { list ->
            list.map { FavouriteTransitDataWithTimeAndSelection(
                it.transitDataWithTime,
                false
            )}
        }
    }

    private fun cancelRemoval(){
        //FIXME isnt even necessary to call this?
        _favouriteTransitData.update { list ->
            list.map { FavouriteTransitDataWithTimeAndSelection(
                it.transitDataWithTime,
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
                        favouritesUseCases.removeFavourite(favouriteTransitData.transitDataWithTime.favouriteTransitData)
                        _favouriteTransitData.update {
                            val list = it.toMutableList()
                            list.removeIf { data ->
                                data.transitDataWithTime.favouriteTransitData ==
                                        favouriteTransitData.transitDataWithTime
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