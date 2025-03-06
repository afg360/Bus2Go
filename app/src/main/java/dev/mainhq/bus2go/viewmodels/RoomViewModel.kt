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
import dev.mainhq.bus2go.database.stm_data.dao.BusRouteInfo
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.FuzzyQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val stmDatabase = Room.databaseBuilder(application, AppDatabaseSTM::class.java, "stm_info.db")
                                .createFromAsset("database/stm_info.db")
                                .addMigrations(AppDatabaseSTM.MIGRATION_1_2)
                                .build()

    //FIXME doing the "today" queries seem to disrupt outputs?
    private val exoDataBase = Room.databaseBuilder(application, AppDatabaseExo::class.java, "exo_info.db")
                                .createFromAsset("database/exo_info.db")
                                .addMigrations(AppDatabaseExo.MIGRATION_1_2)
                                .build()

    suspend fun getFavouriteStopTimes(list : List<TransitData>, agency : TransitAgency,
                                      time : Time, times : MutableList<FavouriteTransitInfo>) : MutableList<FavouriteTransitInfo> {
        when(agency){
            TransitAgency.STM -> {
                list as List<StmBusData>
                val stopsInfoDAO = stmDatabase.stopsInfoDao()
                list.forEach {busInfo ->
                    times.add(
                        FavouriteTransitInfo(
                            busInfo,
                            stopsInfoDAO.getFavouriteStopTime(busInfo.stopName, time.getDayString(), time.getTimeString(), busInfo.direction, busInfo.routeId.toInt(), time.getTodayString()),
                            agency
                        )
                    )
                }
                return times
            }
            TransitAgency.EXO_TRAIN -> {
                list as List<TrainData>
                val stopsTimesDAO = exoDataBase.stopTimesDao()
                list.forEach {trainInfo ->
                    stopsTimesDAO.getFavouriteTrainStopTime("trains-${trainInfo.routeId}", trainInfo.stopName, trainInfo.directionId, time.getTimeString(), time.getDayString(), time.getTodayString())
                        .also { time -> times.add(FavouriteTransitInfo(trainInfo, time, agency)) }
                }
                return times
            }
            TransitAgency.EXO_OTHER -> {
                list as List<ExoBusData>
                val stopTimesDAO = exoDataBase.stopTimesDao()
                list.forEach {busInfo ->
                    stopTimesDAO.getFavouriteBusStopTime(busInfo.stopName, time.getDayString(), time.getTimeString(), busInfo.headsign, time.getTodayString())
                        .also { time -> times.add(FavouriteTransitInfo(busInfo, time, agency)) }
                }
                return times
            }
        }
    }

    /** Use to not directly use the for loop like in the getFavouriteStopTimes method. Only used for the internet connectivity shit */
    suspend fun getFavouriteStopTime(transitData : TransitData, agency : TransitAgency, time : Time) : FavouriteTransitInfo {
        //must be a string of form YYYYMMDD
        return when(agency){
            TransitAgency.STM -> {
                val stopsInfoDAO = stmDatabase.stopsInfoDao()
                FavouriteTransitInfo(transitData,
                    stopsInfoDAO.getFavouriteStopTime(transitData.stopName, time.getDayString(), time.getTimeString(), transitData.direction, transitData.routeId.toInt(), time.getTodayString()), agency)
            }
            TransitAgency.EXO_TRAIN -> {
                val stopsTimesDAO = exoDataBase.stopTimesDao()
                val trainInfo = transitData as TrainData
                FavouriteTransitInfo(transitData,
                    stopsTimesDAO.getFavouriteTrainStopTime("trains-${trainInfo.routeId}",
                        trainInfo.stopName, trainInfo.directionId, time.getTimeString(), time.getDayString(), time.getTodayString()), agency)
            }
            TransitAgency.EXO_OTHER -> {
                val stopTimesDAO = exoDataBase.stopTimesDao()
                val busInfo = transitData as ExoBusData
                FavouriteTransitInfo(transitData,
                    stopTimesDAO.getFavouriteBusStopTime(busInfo.stopName, time.getDayString(), time.getTimeString(), busInfo.routeId, time.getTodayString()), agency)
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

    suspend fun getStmStopTimes(stopName : String, time: Time, headsign : String, routeId: Int) : List<Time>{
        return stmDatabase.stopsInfoDao().getStopTimes(stopName, time.getDayString(), time.getTimeString(), headsign, routeId, time.getTodayString())
    }

    suspend fun getExoOtherStopTimes(stopName : String, time: Time, headsign : String) : List<Time>{
        return exoDataBase.stopTimesDao().getStopTimes(stopName, time.getDayString(), time.getTimeString(), headsign)
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