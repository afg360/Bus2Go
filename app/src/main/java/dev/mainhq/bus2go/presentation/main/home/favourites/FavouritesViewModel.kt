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


    private val _favouriteTransitData: MutableStateFlow<List<TransitDataWithTime>> = MutableStateFlow(listOf())
    val favouriteTransitData get() = _favouriteTransitData.asStateFlow()

    private val _favouritesToRemove: MutableStateFlow<List<TransitData>> = MutableStateFlow(listOf())
    val favouritesToRemove get() = _favouritesToRemove.asStateFlow()

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
                _favouriteTransitData.value = favouritesUseCases.getFavouritesWithTimeData()
                delay(5000)
            }
        }
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
            _favouriteTransitData.value.map { it.favouriteTransitData }
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
            val jobs = _favouritesToRemove.value
                .map { favouriteTransitData ->
                    async {
                        favouritesUseCases.removeFavourite(favouriteTransitData)
                        _favouriteTransitData.update {
                            val list = it.toMutableList()
                            list.removeIf { data ->
                                data.favouriteTransitData == favouriteTransitData
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