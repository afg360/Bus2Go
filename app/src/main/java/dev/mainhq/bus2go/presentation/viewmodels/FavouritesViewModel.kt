package dev.mainhq.bus2go.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.use_case.FavouritesUseCases
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class FavouritesViewModel(private val favouritesUseCases: FavouritesUseCases) : ViewModel(){

    //TODO
    //1) test the whole thing
    //2) add observer to check for click events
    //3) eventually some sort of sorting/categorisation of favourites

    //TODO
    //Perhaps this is also where i should actually keep a list of the items i want to delete
    //not in a list in the ui...

    private val _favouriteTransitData: MutableStateFlow<List<FavouriteTransitData>> = MutableStateFlow(listOf())
    val favouriteTransitData: StateFlow<List<FavouriteTransitData>> get() = _favouriteTransitData

    private val _favouritesToDelete: MutableStateFlow<List<FavouriteTransitData>> = MutableStateFlow(listOf())

    //changes between selection mode for removing favourites and shit, or normal mode where we can click
    private val _selectionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun activateSelectionMode(){
        _selectionMode.value = true
    }

    fun deactivateSelectionMode(){
        _selectionMode.value = false
    }

    fun removeFavourites(toRemoveList : List<FavouriteTransitData>){
        viewModelScope.launch {
            val jobs = toRemoveList.map { favouriteTransitData ->
                async {
                    favouritesUseCases.removeFavourite(favouriteTransitData)
                    _favouriteTransitData.update {
                        val list = it.toMutableList()
                        list.remove(favouriteTransitData)
                        return@update list
                    }
                }
            }
            jobs.forEach { it.await() }
        }
    }

    //TODO what if we already have the same data...?
    fun addFavourite(data : FavouriteTransitData){
        viewModelScope.launch {
            favouritesUseCases.addFavourite(data)
            _favouriteTransitData.update {
                val list = it.toMutableList()
                list.add(data)
                return@update list
            }
        }
    }

    override fun onCleared() {
        //in case we need to reset the mutableStateFlows
        super.onCleared()
    }

}