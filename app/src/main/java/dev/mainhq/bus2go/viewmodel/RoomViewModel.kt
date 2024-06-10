package dev.mainhq.bus2go.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import dev.mainhq.bus2go.database.exo_data.AppDatabaseExo
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.fragments.FavouriteBusInfo
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.utils.BusAgency
import dev.mainhq.bus2go.utils.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.icu.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val stmDatabase : AppDatabaseSTM = Room.databaseBuilder(application, AppDatabaseSTM::class.java, "stm_info.db")
    .createFromAsset("database/stm_info.db").build()

    private val exoDataBase : AppDatabaseExo = Room.databaseBuilder(application, AppDatabaseExo::class.java, "exo_info.db")
        .createFromAsset("database/exo_info.db").build()

    suspend fun getFavouriteStopTimes(list : List<BusInfo>, agency : BusAgency, dayString : String,
                                      calendar : Calendar, times : MutableList<FavouriteBusInfo>) : MutableList<FavouriteBusInfo> {
        when(agency){
            BusAgency.STM -> {
                val stopsInfoDAO = stmDatabase.stopsInfoDao()
                list.forEach {busInfo ->
                    stopsInfoDAO.getFavouriteStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.tripHeadsign)
                        .also { time -> times.add(FavouriteBusInfo(busInfo, time, agency)) }
                }
                return times
            }
            BusAgency.EXO -> {
                val stopTimesDAO = exoDataBase.stopTimesDao()
                list.forEach {busInfo ->
                    stopTimesDAO.getFavouriteStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.tripHeadsign)
                        .also { time -> times.add(FavouriteBusInfo(busInfo, time, agency)) }
                }
                return times
            }
        }
    }

    /** Return the jobs made from the async calls */
    suspend fun getStopNames(coroutineScope: CoroutineScope, agency: BusAgency, dirs : List<String>)
    : Pair<Deferred<List<String>>, Deferred<List<String>>>{
        return when(agency){
            BusAgency.STM -> {
                val exe0 = coroutineScope.async { stmDatabase.stopsInfoDao().getStopNames(dirs[0]) }
                val exe1 = coroutineScope.async { stmDatabase.stopsInfoDao().getStopNames(dirs.last()) }
                Pair(exe0, exe1)
            }

            BusAgency.EXO -> {
                val exe0 = coroutineScope.async { exoDataBase.stopTimesDao().getStopNames(dirs[0]) }
                val exe1 = coroutineScope.async { exoDataBase.stopTimesDao().getStopNames(dirs.last()) }
                Pair(exe0, exe1)
            }
        }
    }

    /** Used for alarm creations */
    suspend fun getStopTimes(stopName : String, dayString : String, headsign : String, agency : BusAgency) : List<Time>{
        return when(agency){
            BusAgency.STM -> stmDatabase.stopsInfoDao().getStopTimes(stopName, dayString, headsign)
            BusAgency.EXO -> exoDataBase.stopTimesDao().getStopTimes(stopName, dayString, headsign)
        }
    }

    /** */
    suspend fun getStopTimes(stopName : String, dayString : String,
                             curTime : String, headsign : String, agency : BusAgency) : List<Time>{
        return when(agency){
            BusAgency.STM -> stmDatabase.stopsInfoDao().getStopTimes(stopName, dayString, curTime, headsign)
            BusAgency.EXO -> exoDataBase.stopTimesDao().getStopTimes(stopName, dayString, curTime, headsign)
        }
    }

    suspend fun getTrips(agency: BusAgency,/** String because some busNums are of the form'T100'*/
                                            bus: String) : List<String>{
        return when(agency){
            BusAgency.STM -> {
                val busNum = bus.toInt()
                if (busNum > 5) stmDatabase.tripsDao().getTripHeadsigns(busNum)
                else listOf()
            }
            BusAgency.EXO -> exoDataBase.tripsDao().getTripHeadsigns(bus)
        }
    }

    override fun onCleared() {
        stmDatabase.close()
        exoDataBase.close()
        super.onCleared()
    }
}