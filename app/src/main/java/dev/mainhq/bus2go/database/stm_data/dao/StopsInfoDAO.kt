package dev.mainhq.bus2go.database.stm_data.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.bus2go.utils.Time

@Dao
interface StopsInfoDAO {

    @Query("SELECT DISTINCT stop_name FROM StopsInfo " +
            "WHERE trip_headsign = (:headsign) " +
            "AND route_id = (:routeId) ORDER BY stop_seq")
    suspend fun getStopNames(headsign : String, routeId : String) : List<String>

    //TODO check if i really need trip_headsign
    @Query("SELECT DISTINCT arrival_time FROM StopsInfo " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:time) AND route_id = (:routeId) " +
            "AND trip_headsign LIKE (:headsign) || '%' " +
            "ORDER BY arrival_time")
    suspend fun getStopTimes(stopName : String, day : String, time : String, /** i.e. DIRECTION string */ headsign: String, routeId: Int) : List<Time>

    /** Used for creating new alarm */
    @Query("SELECT DISTINCT arrival_time FROM StopsInfo " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND trip_headsign = (:headsign) AND route_id = (:routeId) " +
            "ORDER BY arrival_time")
    suspend fun getStopTimes(stopName : String, day : String, headsign: String, routeId: Int) : List<Time>


    @Query("SELECT MIN(arrival_time) FROM STOPSINFO " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:time) AND route_id = (:routeId) " +
            "AND trip_headsign = (:headsign)")
    suspend fun getFavouriteStopTime(stopName : String, day : String, time : String, headsign: String, routeId: Int) : Time

}

