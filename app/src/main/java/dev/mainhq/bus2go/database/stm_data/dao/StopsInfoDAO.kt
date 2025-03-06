package dev.mainhq.bus2go.database.stm_data.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.bus2go.utils.Time

@Dao
/**
 * DAO used to obtain information such as stop names, stop time arrivals, etc.
 **/
interface StopsInfoDAO {

    //FIXME instead of simply creating empty LocalTime, we can create LocalDateTime using the Calendar associated with it

    //query arrival time blabla
    //in the calendar inner query, get the service_id, days and the start/end_date
    //return those shit if it respects the query
    //in the returning thing, if the today < end_date, inside converter use LocalDate.now()

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
    suspend fun getStopTimes(stopName : String, day : String, curTime : String, /** i.e. DIRECTION string */ headsign: String, routeId: Int, curDate: String) : List<Time>

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
            "AND trip_headsign = (:headsign)")
    suspend fun getFavouriteStopTime(stopName : String, day : String, time : String, headsign: String, routeId: Int, curDate: String) : Time?
}