package dev.mainhq.bus2go.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

/*@Dao
interface StopTimesDAO {
    @Query("SELECT DISTINCT arrival_time FROM StopTimes JOIN Trips ON StopTimes.trip_id = Trips.trip_id " +
            "WHERE Trips.trip_headsign LIKE (:headsign) ORDER BY arrival_time;")
    suspend fun getAllArrivalTimesFromBusNum(headsign : String) : List<String>
    //check if we can make better and more efficient queries, or if i should use indexes
    //todo we need to select the trips that have the correct date
    @Query("SELECT DISTINCT stop_name AS stopName,stop_code AS stopCode " +
            "FROM StopTimes AS st JOIN Trips ON st.trip_id = Trips.trip_id " +
            "JOIN Stops AS s ON s.stop_id = st.stop_id " +
            "WHERE Trips.trip_headsign = (:headsign) ORDER BY st.stop_seq;")
    suspend fun getStopInfoFromBusNum(headsign: String) : List<StopInfo>

    //todo may need stopId instead of stopCode
    @Query("SELECT arrival_time AS arrivalTime " +
            "FROM StopTimes JOIN Trips ON StopTimes.trip_id = Trips.trip_id " +
            "JOIN Calendar ON Trips.service_id = Calendar.service_id " +
            "WHERE stop_id = (:stopCode) AND arrival_time >= (:curTime) " +
            "AND Calendar.days LIKE '%' | (:day) | '%'" +
            "ORDER BY arrival_time LIMIT 200;")
    suspend fun getArrivalTimesFromStopCode(stopCode : Int, curTime : String, day : String) : List<String>

    /*@Query("SELECT DISTINCT stop_name AS stopName,arrival_time AS arrivalTime " +
            "FROM stoptimes JOIN trips ON stoptimes.trip_id = trips.trip_id " +
            "JOIN stops on stops.stop_id = stoptimes.stop_id " +
            "JOIN calendar on trips.service_id = calendar.service_id " +
            "WHERE trips.trip_headsign = (:headsign) " +
            "AND calendar.days LIKE '%' | (:day) | '%' " +
            "AND arrival_time >= (:time) " +
            "ORDER BY stoptimes.stop_seq, arrival_time;")
    suspend fun test(headsign : String, day : String, time : String) : List<StopSchedule>*/
}*/

//todo do we need stopCode or stopId??
data class StopInfo(val stopName : String, val stopCode : Int)

//data class StopSchedule(val stopName : String, val arrivalTime : String)