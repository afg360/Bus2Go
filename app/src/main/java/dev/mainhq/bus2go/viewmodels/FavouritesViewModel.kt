package dev.mainhq.bus2go.viewmodels

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.preferences.FavouritesData
import dev.mainhq.bus2go.preferences.SettingsSerializer
import dev.mainhq.bus2go.preferences.TrainInfo
import dev.mainhq.bus2go.utils.BusAgency
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The datastore of favourites refers to favourites defined in the preferences file, at dev.mainhq.schedules.preferences,
 *  NOT THE Favourites.kt FRAGMENT */
val Context.favouritesDataStore : DataStore<FavouritesData> by dataStore(
    fileName = "favourites.json",
    serializer = SettingsSerializer
)

class FavouritesViewModel(private val application: Application) : AndroidViewModel(application){

    private val _stmBusInfo : MutableStateFlow<List<BusInfo>> = MutableStateFlow(listOf())
    val stmBusInfo : StateFlow<List<BusInfo>> get() = _stmBusInfo

    private val _exoBusInfo : MutableStateFlow<List<BusInfo>> = MutableStateFlow(listOf())
    val exoBusInfo : StateFlow<List<BusInfo>> get() = _exoBusInfo

    private val _exoTrainInfo : MutableStateFlow<List<TrainInfo>> = MutableStateFlow(listOf())
    val exoTrainInfo : StateFlow<List<TrainInfo>> get() = _exoTrainInfo

    suspend fun loadData(){
        val data = application.favouritesDataStore.data.first()
        _stmBusInfo.value = data.listSTM
        _exoBusInfo.value = data.listExo
        _exoTrainInfo.value = data.listExoTrain
    }

    fun getAllBusInfo() : List<BusInfo> = stmBusInfo.value + exoBusInfo.value

    suspend fun removeFavouriteBuses(toRemoveList : List<BusInfo>){
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
        //update the current fields so that display can be done properly
        loadData()
    }

    suspend fun removeFavouriteTrains(toRemoveList : List<TrainInfo>){
        viewModelScope.launch {
            application.favouritesDataStore.updateData {favourites ->
                favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
                    //maybe add a tripid or some identifier so that it is a unique thing deleted
                    it.removeIf { trainInfo -> toRemoveList.contains(TODO("")) }
                })
            }
        }
        loadData()
    }

    /** Used inside StopListElemsAdapter */
    fun removeFavouriteBuses(agency : BusAgency, data : String, headsign : String){
        viewModelScope.launch {
            application.favouritesDataStore.updateData { favourites ->
                when(agency){
                    BusAgency.STM -> {
                        favourites.copy(listSTM = favourites.listSTM.mutate {
                            //maybe add a tripid or some identifier so that it is a unique thing deleted
                            it.remove(BusInfo(data, headsign))
                        })
                    }
                    BusAgency.EXO_OTHER -> {
                        favourites.copy(listExo = favourites.listExo.mutate {
                            //maybe add a tripid or some identifier so that it is a unique thing deleted
                            it.remove(BusInfo(data, headsign))
                        })
                    }
                    else -> throw IllegalArgumentException("Cannot give another kind of agency to this function")
                }
            }
        }
    }

    fun removeFavouriteTrains(agency : BusAgency, stopName : String, routeId : String, directionId : Int){
        if (agency == BusAgency.EXO_TRAIN) {
            viewModelScope.launch {
                application.favouritesDataStore.updateData {favourites ->
                    favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
                        //maybe add a tripid or some identifier so that it is a unique thing deleted
                        it.remove(TrainInfo(stopName, routeId, directionId))
                    })
                }
            }
        }
        else throw IllegalArgumentException("This function must only be used by trains!")
    }

    fun addFavourites(agency : BusAgency, data : String, headsign : String){
        viewModelScope.launch {
            application.favouritesDataStore.updateData { favourites ->
                when(agency){
                    BusAgency.STM -> {
                        favourites.copy(listSTM = favourites.listSTM.mutate {
                            //maybe add a tripid or some identifier so that it is a unique thing deleted

                            it.add(BusInfo(data, headsign))
                        })
                    }
                    BusAgency.EXO_OTHER -> {
                        favourites.copy(listExo = favourites.listExo.mutate {
                            //maybe add a tripid or some identifier so that it is a unique thing deleted
                            it.add(BusInfo(data, headsign))
                        })
                    }
                    else -> throw IllegalArgumentException("Cannot give another type of agency to this method")
                }
            }
            loadData()
        }
    }

    fun addFavouriteTrains(agency : BusAgency, stopName : String, routeId : String, directionId : Int){
        if (agency == BusAgency.EXO_TRAIN)  {
            viewModelScope.launch {
                application.favouritesDataStore.updateData { favourites ->
                    favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
                        it.add(TrainInfo(stopName, routeId, directionId))
                    })
                }
                loadData()
            }
        }
        else throw IllegalArgumentException("Cannot call this method on a non-train agency")
    }
}