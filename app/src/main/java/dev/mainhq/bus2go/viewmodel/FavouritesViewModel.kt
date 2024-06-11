package dev.mainhq.bus2go.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.preferences.FavouritesData
import dev.mainhq.bus2go.preferences.SettingsSerializer
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

    private val _exoTrainInfo : MutableStateFlow<List<BusInfo>> = MutableStateFlow(listOf())
    val exoTrainInfo : StateFlow<List<BusInfo>> get() = _exoTrainInfo

    suspend fun loadData(){
        val data = application.favouritesDataStore.data.first()
        _stmBusInfo.value = data.listSTM
        _exoBusInfo.value = data.listExo
        _exoTrainInfo.value = data.listExoTrain
    }

    fun getAllBusInfo() : List<BusInfo> = stmBusInfo.value + exoBusInfo.value
    fun getAllInfo() : List<BusInfo> = stmBusInfo.value + exoBusInfo.value + exoTrainInfo.value

    suspend fun removeFavourites(toRemoveList : List<BusInfo>){
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
        application.favouritesDataStore.updateData { favouritesData ->
            favouritesData.copy(listExoTrain = favouritesData.listExoTrain.mutate {
                it.removeIf{busInfo -> toRemoveList.contains(busInfo) }
            })
        }
        //update the current fields so that display can be done properly
        loadData()
    }

    /** Used inside StopListElemsAdapter */
    fun removeFavourites(agency : BusAgency, data : String, headsign : String){
        viewModelScope.launch {
            application.favouritesDataStore.updateData { favourites ->
                when(agency){
                    BusAgency.STM -> {
                        favourites.copy(listSTM = favourites.listSTM.mutate {
                            //maybe add a tripid or some identifier so that it is a unique thing deleted
                            it.remove(BusInfo(data, headsign))
                        })
                    }
                    BusAgency.EXO_TRAIN -> {
                        favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
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
                }
            }
        }
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
                    BusAgency.EXO_TRAIN -> {
                        favourites.copy(listExoTrain = favourites.listExoTrain.mutate {
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
                }
            }
        }
    }

}