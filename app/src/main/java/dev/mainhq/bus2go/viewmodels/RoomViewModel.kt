package dev.mainhq.bus2go.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import dev.mainhq.bus2go.database.exo_data.AppDatabaseExo
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.fragments.FavouriteTransitInfo
import dev.mainhq.bus2go.preferences.ExoBusData
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.utils.Time
import android.icu.util.Calendar
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.database.stm_data.dao.BusRouteInfo
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.getDayString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val stmDatabase : AppDatabaseSTM = Room.databaseBuilder(application, AppDatabaseSTM::class.java, "stm_info.db")
        .createFromAsset("database/stm_info.db")
        .addMigrations(AppDatabaseSTM.MIGRATION_1_2)
        .build()

    //FIXME no migration for exo yet, next time must set a new migration
    private val exoDataBase : AppDatabaseExo = Room.databaseBuilder(application, AppDatabaseExo::class.java, "exo_info.db")
        .createFromAsset("database/exo_info.db").build()

    suspend fun getFavouriteStopTimes(list : List<TransitData>, agency : TransitAgency, dayString : String,
                                      calendar : Calendar, times : MutableList<FavouriteTransitInfo>) : MutableList<FavouriteTransitInfo> {
        //must be a string of form YYYYMMDD
        val today = "${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.MONTH)}${calendar.get(Calendar.DAY_OF_MONTH)}"
        when(agency){
            TransitAgency.STM -> {
                list as List<StmBusData>
                val stopsInfoDAO = stmDatabase.stopsInfoDao()
                list.forEach {busInfo ->
                    stopsInfoDAO.getFavouriteStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.direction, busInfo.routeId.toInt(), today)
                        .also { time -> times.add(FavouriteTransitInfo(busInfo, time, agency)) }
                }
                return times
            }
            TransitAgency.EXO_TRAIN -> {
                list as List<TrainData>
                val stopsTimesDAO = exoDataBase.stopTimesDao()
                list.forEach {trainInfo ->
                    stopsTimesDAO.getFavouriteTrainStopTime("trains-${trainInfo.routeId}", trainInfo.stopName, trainInfo.directionId, Time(calendar).toString(), dayString)
                        .also { time -> times.add(FavouriteTransitInfo(trainInfo, time, agency)) }
                }
                return times
            }
            TransitAgency.EXO_OTHER -> {
                list as List<ExoBusData>
                val stopTimesDAO = exoDataBase.stopTimesDao()
                list.forEach {busInfo ->
                    stopTimesDAO.getFavouriteBusStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.routeId)
                        .also { time -> times.add(FavouriteTransitInfo(busInfo, time, agency)) }
                }
                return times
            }
        }
    }
    
    /** Use to not directly use the for loop like in the getFavouriteStopTimes method. Only used for the internet connectivity shit */
    suspend fun getFavouriteStopTime(transitData : TransitData, agency : TransitAgency, dayString : String,
                                      calendar : Calendar) : FavouriteTransitInfo {
        //must be a string of form YYYYMMDD
        val today = "${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.MONTH)}${calendar.get(Calendar.DAY_OF_MONTH)}"
        return when(agency){
            TransitAgency.STM -> {
                val stopsInfoDAO = stmDatabase.stopsInfoDao()
                FavouriteTransitInfo(transitData,
                    stopsInfoDAO.getFavouriteStopTime(transitData.stopName, dayString, Time(calendar).toString(), transitData.direction, transitData.routeId.toInt(), today), agency)
            }
            TransitAgency.EXO_TRAIN -> {
                val stopsTimesDAO = exoDataBase.stopTimesDao()
                val trainInfo = transitData as TrainData
                FavouriteTransitInfo(transitData,
                    stopsTimesDAO.getFavouriteTrainStopTime("trains-${trainInfo.routeId}",
                        trainInfo.stopName, trainInfo.directionId, Time(calendar).toString(), dayString), agency)
            }
            TransitAgency.EXO_OTHER -> {
                val stopTimesDAO = exoDataBase.stopTimesDao()
                val busInfo = transitData as ExoBusData
                FavouriteTransitInfo(transitData,
                    stopTimesDAO.getFavouriteBusStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.routeId), agency)
            }
        }
    }

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
    /** Used for alarm creations */
    suspend fun getStopTimes(stopName : String, calendar : Calendar, headsign : String, agency : TransitAgency, routeId: Int?, today: String) : List<Time>{
        val dayString = getDayString(calendar)
        return when(agency){
            TransitAgency.STM -> stmDatabase.stopsInfoDao().getStopTimes(stopName, dayString, headsign, routeId!!, today)
            TransitAgency.EXO_OTHER -> exoDataBase.stopTimesDao().getStopTimes(stopName, dayString, headsign)
            else -> throw IllegalArgumentException("Cannot use the fxn getStopTimes for ${agency}.")
        }
    }

    /** */
    suspend fun getStopTimes(stopName : String, calendar: Calendar, headsign : String,
                             agency : TransitAgency, routeId: Int?) : List<Time>{
        val curTime = Time(calendar).toString()
        val dayString = getDayString(calendar)
        //must be a string of form YYYYMMDD
        val today = "${calendar.get(Calendar.YEAR)}${calendar.get(Calendar.MONTH)}${calendar.get(Calendar.DAY_OF_MONTH)}"
        return when(agency){
            TransitAgency.STM -> stmDatabase.stopsInfoDao().getStopTimes(stopName, dayString, curTime, headsign, routeId!!, today)
            TransitAgency.EXO_OTHER -> exoDataBase.stopTimesDao().getStopTimes(stopName, dayString, curTime, headsign)
            else -> throw IllegalArgumentException("Cannot use the fxn getStopNames for ${agency}.")
        }
    }

    suspend fun getTrainStopTimes(routeId: Int, stopName: String, directionId : Int, calendar: Calendar) : List<Time>{
        return exoDataBase.stopTimesDao().getTrainStopTimes("trains-$routeId", stopName, directionId, Time(calendar).toString(), getDayString(calendar))
    }

    //FIXME TEMPORARY SOLUTION
    suspend fun getDirections(agency: TransitAgency,/** String because some busNums are of the form'T100'*/
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

    suspend fun queryStmRoutes(query: FuzzyQuery) : List<BusRouteInfo> {
        return stmDatabase.routesDao().getBusRouteInfo(query)
    }

    suspend fun queryExoRoutes(query: FuzzyQuery) : List<BusRouteInfo> {
        return exoDataBase.routesDao().getBusRouteInfo(query)
    }
    /** For STM testing only for now */
    suspend fun getNames(stopId : Int) : String{
        return stmDatabase.stopDao().getStopName(stopId)
    }

    override fun onCleared() {
        stmDatabase.close()
        exoDataBase.close()
        super.onCleared()
    }
}