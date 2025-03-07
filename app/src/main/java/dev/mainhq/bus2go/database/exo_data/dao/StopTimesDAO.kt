package dev.mainhq.bus2go.database.exo_data.dao;

import androidx.room.Dao;
import androidx.room.Query;
import dev.mainhq.bus2go.utils.Time

@Dao
//FIXME doing today queries instead of calendar seem to disrupt everything...
interface StopTimesDAO {

    //TODO Add query for old data

    @Query("SELECT DISTINCT stop_name FROM StopTimes " +
            "JOIN Stops on Stops.stop_id = StopTimes.stop_id " +
            "JOIN Trips ON Trips.trip_id = StopTimes.trip_id " +
            "WHERE trip_headsign = (:headsign) ORDER BY stop_seq")
    suspend fun getStopNames(headsign : String) : List<String>

    @Query("SELECT DISTINCT stop_name FROM StopTimes " +
            "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips on StopTimes.trip_id = Trips.trip_id " +
            "WHERE route_id = (:routeId) " +
            "AND direction_id = (:directionId) ORDER BY stop_seq;")
    suspend fun getTrainStopNames(routeId : String, directionId : Int) : List<String>

    @Query("SELECT DISTINCT arrival_time FROM StopTimes " +
            "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips ON StopTimes.trip_id = Trips.trip_id " +
            "JOIN " +
            "(SELECT service_id, days FROM Calendar " +
            "WHERE Calendar.start_date <= (:curDate) AND Calendar.end_date >= (:curDate)" +
            ") AS Calendar " +
            "ON Calendar.service_id = Trips.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:curTime) AND trip_headsign = (:headsign) " +
            "ORDER BY arrival_time")
    //perhaps also use the agency as a search argument
    suspend fun getStopTimes(stopName : String, day : String, curTime : String, headsign: String, curDate: String) : List<Time>

    /*
    //TODO To use
    @Query("SELECT DISTINCT arrival_time FROM StopTimes " +
            "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips ON StopTimes.trip_id = Trips.trip_id " +
            "JOIN " +
            "(SELECT service_id, days FROM Calendar " +
            "WHERE Calendar.end_date = (SELECT MAX(end_date) FROM Calendar)" +
            ") AS Calendar " +
            "ON Calendar.service_id = Trips.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:curTime) AND trip_headsign = (:headsign) " +
            "ORDER BY arrival_time")
    suspend fun getOldStopTimes(stopName : String, day : String, curTime : String, headsign: String) : List<Time>
    */

    @Query("SELECT MIN(arrival_time) AS arrival_time FROM " +
                "(SELECT arrival_time FROM StopTimes " +
                "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
                "JOIN Trips on StopTimes.trip_id = Trips.trip_id " +
                "JOIN " +
                "(SELECT service_id, days FROM Calendar " +
                "WHERE Calendar.start_date <= (:curDate) AND Calendar.end_date >= (:curDate)" +
                ") AS Calendar " +
                "ON Calendar.service_id = Trips.service_id " +
                "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
                "AND arrival_time >= (:curTime) AND trip_headsign = (:headsign)" +
            ")")
    suspend fun getFavouriteBusStopTime(stopName : String, day : String, curTime : String, headsign: String, curDate: String) : Time?

    @Query("SELECT arrival_time FROM StopTimes " +
            "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips ON Stoptimes.trip_id = Trips.trip_id " +
            "JOIN " +
            "(SELECT service_id, days FROM Calendar " +
            "WHERE Calendar.start_date <= (:curDate) AND Calendar.end_date >= (:curDate)" +
            ") AS Calendar " +
            "ON Calendar.service_id = Trips.service_id " +
            "WHERE route_id = (:routeId) " +
            "AND direction_id = (:directionId) AND arrival_time >= (:time) " +
            "AND stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "ORDER BY stoptimes.stop_seq,arrival_time;")
    suspend fun getTrainStopTimes(routeId: String, stopName: String, directionId: Int, time: String, day : String, curDate: String) : List<Time>

    @Query("SELECT MIN(arrival_time) FROM StopTimes " +
            "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips ON Stoptimes.trip_id = Trips.trip_id " +
            "JOIN " +
            "(SELECT service_id, days FROM Calendar " +
            "WHERE Calendar.start_date <= (:curDate) AND Calendar.end_date >= (:curDate)" +
            ") AS Calendar " +
            "ON Calendar.service_id = Trips.service_id " +
            "WHERE route_id = (:routeId) " +
            "AND direction_id = (:directionId) AND arrival_time >= (:time) " +
            "AND stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "ORDER BY stoptimes.stop_seq,arrival_time;")
    suspend fun getFavouriteTrainStopTime(routeId: String, stopName: String, directionId: Int, time: String, day : String, curDate: String) : Time?
}