package dev.mainhq.bus2go.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import dev.mainhq.bus2go.database.exo_data.AppDatabaseExo
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.fragments.FavouriteTransitInfo
import dev.mainhq.bus2go.preferences.BusData
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.utils.Time
import android.icu.util.Calendar
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.preferences.TransitData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val stmDatabase : AppDatabaseSTM = Room.databaseBuilder(application, AppDatabaseSTM::class.java, "stm_info.db")
    .createFromAsset("database/stm_info.db").build()

    private val exoDataBase : AppDatabaseExo = Room.databaseBuilder(application, AppDatabaseExo::class.java, "exo_info.db")
        .createFromAsset("database/exo_info.db").build()

    suspend fun getFavouriteStopTimes(list : List<TransitData>, agency : TransitAgency, dayString : String,
                                      calendar : Calendar, times : MutableList<FavouriteTransitInfo>) : MutableList<FavouriteTransitInfo> {
        when(agency){
            TransitAgency.STM -> {
                list as List<BusData>
                val stopsInfoDAO = stmDatabase.stopsInfoDao()
                list.forEach {busInfo ->
                    stopsInfoDAO.getFavouriteStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.tripHeadsign)
                        .also { time -> times.add(FavouriteTransitInfo(busInfo, time, agency)) }
                }
                return times
            }
            TransitAgency.EXO_TRAIN -> {
                list as List<TrainData>
                val stopsTimesDAO = exoDataBase.stopTimesDao()
                list.forEach {trainInfo ->
                    stopsTimesDAO.getFavouriteTrainStopTime(trainInfo.routeId, trainInfo.stopName, trainInfo.directionId, Time(calendar).toString(), dayString)
                        .also { time -> times.add(FavouriteTransitInfo(trainInfo, time, agency)) }
                }
                return times
            }
            TransitAgency.EXO_OTHER -> {
                list as List<BusData>
                val stopTimesDAO = exoDataBase.stopTimesDao()
                list.forEach {busInfo ->
                    stopTimesDAO.getFavouriteBusStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.tripHeadsign)
                        .also { time -> times.add(FavouriteTransitInfo(busInfo, time, agency)) }
                }
                return times
            }
        }
    }

    /** Return the jobs made from the async calls */
    suspend fun getStopNames(coroutineScope: CoroutineScope, agency: TransitAgency, dirs : List<String>)
    : Pair<Deferred<List<String>>, Deferred<List<String>>>{
        return when(agency){
            TransitAgency.STM -> {
                val exe0 = coroutineScope.async { stmDatabase.stopsInfoDao().getStopNames(dirs[0]) }
                val exe1 = coroutineScope.async { stmDatabase.stopsInfoDao().getStopNames(dirs.last()) }
                Pair(exe0, exe1)
            }
            TransitAgency.EXO_OTHER -> {
                val exe0 = coroutineScope.async { exoDataBase.stopTimesDao().getStopNames(dirs[0]) }
                val exe1 = coroutineScope.async { exoDataBase.stopTimesDao().getStopNames(dirs.last()) }
                Pair(exe0, exe1)
            }
            else -> throw IllegalArgumentException("Cannot use the fxn getStopNames for ${agency}.")
        }
    }

    /** Used for alarm creations */
    suspend fun getStopTimes(stopName : String, dayString : String, headsign : String, agency : TransitAgency) : List<Time>{
        return when(agency){
            TransitAgency.STM -> stmDatabase.stopsInfoDao().getStopTimes(stopName, dayString, headsign)
            TransitAgency.EXO_OTHER -> exoDataBase.stopTimesDao().getStopTimes(stopName, dayString, headsign)
            else -> throw IllegalArgumentException("Cannot use the fxn getStopTimes for ${agency}.")
        }
    }

    /** */
    suspend fun getStopTimes(stopName : String, dayString : String,
                             curTime : String, headsign : String, agency : TransitAgency) : List<Time>{
        return when(agency){
            TransitAgency.STM -> stmDatabase.stopsInfoDao().getStopTimes(stopName, dayString, curTime, headsign)
            TransitAgency.EXO_OTHER -> exoDataBase.stopTimesDao().getStopTimes(stopName, dayString, curTime, headsign)
            else -> throw IllegalArgumentException("Cannot use the fxn getStopNames for ${agency}.")
        }
    }

    suspend fun getTrainStopTimes(routeId: Int, stopName: String, directionId : Int, curTime : String, dayString: String) : List<Time>{
        return exoDataBase.stopTimesDao().getTrainStopTimes(routeId, stopName, directionId, curTime, dayString)
    }

    suspend fun getDirections(agency: TransitAgency,/** String because some busNums are of the form'T100'*/
                                            bus: String) : List<String>{
        return when(agency){
            TransitAgency.STM -> {
                val busNum = bus.toInt()
                if (busNum > 5) stmDatabase.tripsDao().getTripHeadsigns(busNum)
                else listOf()
            }
            TransitAgency.EXO_TRAIN -> throw IllegalArgumentException("Cannot use the fxn getDirections for ${agency}.")
            TransitAgency.EXO_OTHER -> exoDataBase.tripsDao().getTripHeadsigns(bus)
        }
    }

    //FIXME may need direction_id in the arguments...?
    suspend fun getTrainStopNames(coroutineScope: CoroutineScope, routeId : Int)
    : Pair<Deferred<List<String>>, Deferred<List<String>>> {
        val job1 = coroutineScope.async { exoDataBase.stopTimesDao().getTrainStopNames(routeId, 0) }
        val job2 = coroutineScope.async { exoDataBase.stopTimesDao().getTrainStopNames(routeId, 1) }
        return Pair(job1, job2)
    }

    override fun onCleared() {
        stmDatabase.close()
        exoDataBase.close()
        super.onCleared()
    }
}