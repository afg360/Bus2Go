package dev.mainhq.bus2go.database.exo_data.dao;

import androidx.room.Dao;
import androidx.room.Query;
import dev.mainhq.bus2go.utils.Time

@Dao
interface StopTimesDAO {

    @Query("SELECT DISTINCT stop_name FROM StopTimes " +
            "JOIN Stops on Stops.id = StopTimes.stop_id " +
            "JOIN Trips ON Trips.trip_id = StopTimes.trip_id " +
            "WHERE trip_headsign = (:headsign) ORDER BY stop_seq")
    suspend fun getStopNames(headsign : String) : List<String>

    @Query("SELECT DISTINCT arrival_time FROM StopTimes " +
            "JOIN  Stops ON StopTimes.stop_id = Stops.id " +
            "JOIN Trips ON StopTimes.trip_id = Trips.trip_id " +
            "JOIN Calendar ON Calendar.service_id = Trips.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:time) AND trip_headsign = (:headsign) " +
            "ORDER BY arrival_time")
    //perhaps also use the agency as a search argument
    suspend fun getStopTimes(stopName : String, day : String, time : String, headsign: String) : List<Time>

    /*@Query("SELECT DISTINCT stop_name AS stopName,arrival_time AS arrivalTime " +
            "FROM stoptimes JOIN trips ON stoptimes.trip_id = trips.trip_id " +
            "JOIN stops on stops.stop_id = stoptimes.stop_id " +
            "JOIN calendar on trips.service_id = calendar.service_id " +
            "WHERE trips.trip_headsign = (:headsign) " +
            "AND calendar.days LIKE '%' | (:day) | '%' " +
            "AND arrival_time >= (:time) " +
            "ORDER BY stoptimes.stop_seq, arrival_time;")
    suspend fun test(headsign : String, day : String, time : String) : List<StopSchedule>*/
}

//data class StopSchedule(val stopName : String, val arrivalTime : String)