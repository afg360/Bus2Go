package dev.mainhq.bus2go.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoBusData
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.StmBusData
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoTrainData
import dev.mainhq.bus2go.preferences.TransitData
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class FavouritesViewModel(private val application: Application) : AndroidViewModel(application){

    private val _stmBusInfo : MutableStateFlow<List<StmBusData>> = MutableStateFlow(listOf())
    val stmBusInfo : StateFlow<List<StmBusData>> get() = _stmBusInfo

    private val _exoBusInfo : MutableStateFlow<List<ExoBusData>> = MutableStateFlow(listOf())
    val exoBusInfo : StateFlow<List<ExoBusData>> get() = _exoBusInfo

    private val _exoTrainInfo : MutableStateFlow<List<ExoTrainData>> = MutableStateFlow(listOf())
    val exoTrainInfo : StateFlow<List<ExoTrainData>> get() = _exoTrainInfo


    /** Loads the data stored in favourites.json datastore file */
    suspend fun loadData(){
        val data = application.favouritesDataStore.data.first()
        _stmBusInfo.value = data.listSTM
        _exoBusInfo.value = data.listExo
        _exoTrainInfo.value = data.listExoTrain
    }

    fun getAllBusInfo() = (stmBusInfo.value + exoBusInfo.value + exoTrainInfo.value) as List<TransitData>


    suspend fun removeFavourites(toRemoveList : List<TransitData>){
        //update data inside the json file
        application.favouritesDataStore.updateData { favouritesData ->
            favouritesData.copy(listSTM = favouritesData.listSTM.mutate {
                it.removeIf{busInfo -> toRemoveList.contains(busInfo) }
            })
        }
        application.favouritesDataStore.updateData { favouritesData ->
            favouritesData.copy(listExo = favouritesData.listExo.mutate {
                it.removeIf{busInfo -> toRemoveList.contains(busInfo) }
            })
        }
        application.favouritesDataStore.updateData {favourites ->
            favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
                //maybe add a tripid or some identifier so that it is a unique thing deleted
                it.removeIf { trainInfo -> toRemoveList.contains(trainInfo) }
            })
        }
        //update the current fields so that display can be done properly
        loadData()
    }


    /** Used inside StopListElemsAdapter */
    fun removeFavourites(data : ExoBusData){
        viewModelScope.launch {
            application.favouritesDataStore.updateData { favourites ->
                favourites.copy(listExo = favourites.listExo.mutate {
                    it.remove(data)
                })
            }
        }
    }

    fun removeFavourites(data : StmBusData){
        viewModelScope.launch {
            application.favouritesDataStore.updateData { favourites ->
                favourites.copy(listSTM = favourites.listSTM.mutate {
                    //maybe add a tripid or some identifier so that it is a unique thing deleted
                    it.remove(data)
                })
            }
        }
    }

    fun removeFavourites(data : ExoTrainData){
        viewModelScope.launch {
            application.favouritesDataStore.updateData {favourites ->
                favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
                    //maybe add a tripid or some identifier so that it is a unique thing deleted
                    it.remove(data)
                })
            }
        }
    }

    fun addFavourites(data : StmBusData){
        viewModelScope.launch {
            application.favouritesDataStore.updateData { favourites ->
                favourites.copy(listSTM = favourites.listSTM.mutate {
                    //maybe add a tripid or some identifier so that it is a unique thing deleted
                    it.add(data)
                })
            }
            _stmBusInfo.value = application.favouritesDataStore.data.first().listSTM
        }
    }

    fun addFavourites(data : ExoBusData){
        viewModelScope.launch {
            application.favouritesDataStore.updateData {favourites ->
                favourites.copy(listExo = favourites.listExo.mutate {
                    //maybe add a tripid or some identifier so that it is a unique thing deleted
                    it.add(data)
                })
            }
            _exoBusInfo.value = application.favouritesDataStore.data.first().listExo
        }
    }

    fun addFavourites(data : ExoTrainData){
        viewModelScope.launch {
            application.favouritesDataStore.updateData { favourites ->
                favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
                    it.add(data)
                })
            }
            _exoTrainInfo.value = application.favouritesDataStore.data.first().listExoTrain
        }
    }
}