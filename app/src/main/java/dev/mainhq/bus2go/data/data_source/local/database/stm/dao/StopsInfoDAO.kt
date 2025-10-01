package dev.mainhq.bus2go.data.data_source.local.database.stm.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.bus2go.domain.entity.Time

@Dao
/**
 * DAO used to obtain information such as stop names, stop time arrivals, etc.
 **/
interface StopsInfoDAO {

    //TODO add a query for old data

    @Query("SELECT DISTINCT stop_name FROM StopsInfo " +
            "WHERE trip_headsign = (:headsign) " +
            "AND route_id = (:routeId) ORDER BY stop_seq")
    suspend fun getStopNames(headsign : String, routeId : String) : List<String>

    @Query("SELECT DISTINCT arrival_time FROM StopsInfo JOIN " +
            "(SELECT service_id, days FROM Calendar " +
            "WHERE Calendar.start_date <= (:curDate) AND Calendar.end_date >= (:curDate)" +
            ") AS Calendar " +
            "ON StopsInfo.service_id = Calendar.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:curTime) AND route_id = (:routeId) " +
            "AND trip_headsign LIKE (:headsign) || '%' " +
            "ORDER BY arrival_time")
    /** @param headsign i.e. DIRECTION string */
    suspend fun getStopTimes(stopName : String, day : String, curTime : String, headsign: String, routeId: Int, curDate: String) : List<Time>

    @Query("SELECT DISTINCT arrival_time FROM StopsInfo JOIN " +
            "(SELECT service_id, days FROM Calendar " +
            "WHERE Calendar.end_date = (SELECT MAX(end_date) FROM Calendar) " +
            ") AS Calendar " +
            "ON StopsInfo.service_id = Calendar.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:curTime) AND route_id = (:routeId) " +
            "AND trip_headsign LIKE (:headsign) || '%' " +
            "ORDER BY arrival_time")
    suspend fun getOldTimes(stopName: String, day: String, curTime: String, headsign: String, routeId: String): List<Time>

    /** Used for creating new alarm */
    @Query("SELECT DISTINCT arrival_time FROM StopsInfo JOIN " +
            "(SELECT service_id, days FROM Calendar " +
            "WHERE Calendar.start_date <= (:curDate) AND Calendar.end_date >= (:curDate)" +
            ") AS Calendar " +
            "ON StopsInfo.service_id = Calendar.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND trip_headsign LIKE (:headsign) || '%' " +
            "AND route_id = (:routeId) " +
            "ORDER BY arrival_time;" )
    suspend fun getStopTimes(stopName : String, day : String, headsign: String, routeId: Int, curDate: String) : List<Time>


    @Query("SELECT MIN(arrival_time) FROM StopsInfo JOIN " +
            "(SELECT service_id, days FROM Calendar " +
            "WHERE Calendar.start_date <= (:curDate) AND Calendar.end_date >= (:curDate)" +
            ") AS Calendar " +
            "ON StopsInfo.service_id = Calendar.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:time) AND route_id = (:routeId) " +
            "AND trip_headsign = (:direction)")
    /** @param direction aka headsign **/
    suspend fun getFavouriteStopTime(stopName : String, day : String, time : String, direction: String, routeId: Int, curDate: String) : Time?
}