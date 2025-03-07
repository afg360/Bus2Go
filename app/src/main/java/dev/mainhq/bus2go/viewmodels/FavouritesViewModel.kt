package dev.mainhq.bus2go.viewmodels

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.preferences.ExoBusData
import dev.mainhq.bus2go.preferences.FavouritesData
import dev.mainhq.bus2go.preferences.FavouritesDataOld
import dev.mainhq.bus2go.preferences.SettingsSerializer
import dev.mainhq.bus2go.preferences.SettingsSerializerOld
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.preferences.TransitData
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.prefs.Preferences

/** The datastore of favourites refers to favourites defined in the preferences file, at dev.mainhq.schedules.preferences,
 *  NOT THE Favourites.kt FRAGMENT */
val Context.favouritesDataStoreOld by dataStore(
    fileName = "favourites.json",
    serializer = SettingsSerializerOld,
)

val Context.favouritesDataStore by dataStore(
    fileName = "favourites_1.json",
    serializer = SettingsSerializer,
    produceMigrations = {
        context -> listOf(
            object : DataMigration<FavouritesData> {
                override suspend fun cleanUp() {
                    val oldFile = File(context.filesDir.resolve("datastore"), "favourites.json")
                    if (oldFile.exists()) {
                        oldFile.delete()
                    }
                }

                override suspend fun shouldMigrate(currentData: FavouritesData): Boolean {
                    return File(context.filesDir.resolve("datastore"), "favourites.json").exists()
                }

                override suspend fun migrate(currentData: FavouritesData): FavouritesData {
                    return currentData.copy(
                        listSTM = context.favouritesDataStoreOld.data.first().listSTM,
                        listExo = context.favouritesDataStoreOld.data.first().listExo.toList().map{
                            ExoBusData(
                                stopName = it.stopName,
                                routeId = "",
                                direction = it.direction,
                                routeLongName = "",
                                headsign = it.routeId
                            )
                        }.toPersistentList(),
                        listExoTrain = context.favouritesDataStoreOld.data.first().listExoTrain
                    )
                }

            }
        )
    }
)

class FavouritesViewModel(private val application: Application) : AndroidViewModel(application){

    private val _stmBusInfo : MutableStateFlow<List<StmBusData>> = MutableStateFlow(listOf())
    val stmBusInfo : StateFlow<List<StmBusData>> get() = _stmBusInfo

    private val _exoBusInfo : MutableStateFlow<List<ExoBusData>> = MutableStateFlow(listOf())
    val exoBusInfo : StateFlow<List<ExoBusData>> get() = _exoBusInfo

    private val _exoTrainInfo : MutableStateFlow<List<TrainData>> = MutableStateFlow(listOf())
    val exoTrainInfo : StateFlow<List<TrainData>> get() = _exoTrainInfo

    suspend fun migrateFavouritesData(updateData: List<Pair<String, String>>){
        //FIXME DO IT ONLY WHEN NEEDED
        application.favouritesDataStore.updateData { favouritesData ->
            favouritesData.copy(
                //recreate the list by initialising the data
                listExo = favouritesData.listExo.zip(updateData){ favourite, updatedData ->
                    ExoBusData(favourite.stopName, updatedData.first, favourite.direction, updatedData.second, favourite.headsign)
                }.toPersistentList()
            )
        }
    }

    suspend fun doesNeedMigration(): Boolean {
        if (application.favouritesDataStore.data.first().listExo.isNotEmpty()){
            return application.favouritesDataStore.data.first().listExo.any { it.routeId == "" }
        }
        return false
    }

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

    fun removeFavourites(data : TrainData){
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

    fun addFavourites(data : TrainData){
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