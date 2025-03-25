package dev.mainhq.bus2go.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.domain.entity.FavouriteTransitInfo
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoBusData
import dev.mainhq.bus2go.domain.entity.TransitAgency
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.StmBusData
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoTrainData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.FuzzyQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.time.LocalDate

class RoomViewModel(application: Application) : AndroidViewModel(application) {


    /** Return the jobs made from the async calls, only used with ExoOther when dirs.size == 1 */
    suspend fun getStopNames(coroutineScope: CoroutineScope, dirs : String, routeId: String?)
    : Deferred<List<String>>{
        return coroutineScope.async { exoDataBase.stopTimesDao().getStopNames(dirs) }
    }
    
    /** Return the jobs made from the async calls */
    suspend fun getStopNames(coroutineScope: CoroutineScope, agency: TransitAgency, dirs : List<String>, routeId: String?)
            : Pair<Deferred<List<String>>, Deferred<List<String>>>{
        return when(agency){
            TransitAgency.STM -> {
                //East
                val stm0 = coroutineScope.async { stmDatabase.stopsInfoDao().getStopNames(dirs[0], routeId!!) }
                //West
                val stm1 = coroutineScope.async { stmDatabase.stopsInfoDao().getStopNames(dirs.last(), routeId!!) }
                Pair(stm0, stm1)
            }
            TransitAgency.EXO_OTHER -> {
                val exo0 = coroutineScope.async { exoDataBase.stopTimesDao().getStopNames(dirs[0]) }
                val exo1 = coroutineScope.async { exoDataBase.stopTimesDao().getStopNames(dirs.last()) }
                Pair(exo0, exo1)
            }
            else -> throw IllegalArgumentException("Cannot use the fxn getStopNames for ${agency}.")
        }
    }

    suspend fun getStmStopTimes(stopName : String, time: Time, headsign : String, routeId: Int) : List<Time>{
        return stmDatabase.stopsInfoDao().getStopTimes(stopName, time.getDayString(), time.getTimeString(), headsign, routeId, time.getTodayString())
    }

    suspend fun getExoOtherStopTimes(stopName : String, time: Time, headsign : String) : List<Time>{
        return exoDataBase.stopTimesDao().getStopTimes(stopName, time.getDayString(), time.getTimeString(), headsign, time.getTodayString())
    }

    suspend fun getTrainStopTimes(routeId: Int, stopName: String, directionId : Int, time: Time) : List<Time>{
        return exoDataBase.stopTimesDao().getTrainStopTimes("trains-$routeId", stopName, directionId, time.getTimeString(), time.getDayString(), time.getTodayString())
    }

    /** Fxn that returns a list of DirectionInfo object for Stm,
     *  a list of String representing ExoBus headsigns,
     *  or throws an exception for trains */
    suspend fun getDirections(agency: TransitAgency,/** String because some busNums are of the form 'T100'*/
                                            bus: String) : List<Any>{
        return when(agency){
            TransitAgency.STM -> {
                val busNum = bus.toInt()
                if (busNum > 5) return stmDatabase.tripsDao().getDirectionInfo(busNum)
                else listOf()
            }
            TransitAgency.EXO_TRAIN -> throw IllegalArgumentException("Cannot use the fxn getDirections for ${agency}.")
            TransitAgency.EXO_OTHER -> exoDataBase.tripsDao().getTripHeadsigns(bus)
        }
    }

    //FIXME may need direction_id in the arguments...?
    suspend fun getTrainStopNames(coroutineScope: CoroutineScope, routeId : Int)
    : Pair<Deferred<List<String>>, Deferred<List<String>>> {
        val job1 = coroutineScope.async { exoDataBase.stopTimesDao().getTrainStopNames("trains-$routeId", 0) }
        val job2 = coroutineScope.async { exoDataBase.stopTimesDao().getTrainStopNames("trains-$routeId", 1) }
        return Pair(job1, job2)
    }


    /** For STM testing only for now */
    suspend fun getNames(stopId : Int) : String{
        return stmDatabase.stopDao().getStopName(stopId)
    }

    suspend fun getMigrationData(headsign: String): Pair<String, String>{
        val routeId = exoDataBase.tripsDao().getRouteId(headsign)
        val routeLongName = exoDataBase.routesDao().getBusDir(routeId)
        return Pair(routeId.substringAfter("-"), routeLongName)
    }

    override fun onCleared() {
        stmDatabase.close()
        exoDataBase.close()
        super.onCleared()
    }

}