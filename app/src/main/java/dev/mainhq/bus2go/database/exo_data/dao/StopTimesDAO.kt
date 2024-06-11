package dev.mainhq.bus2go.database.exo_data.dao;

import androidx.room.Dao;
import androidx.room.Query;
import dev.mainhq.bus2go.utils.Time

@Dao
interface StopTimesDAO {

    //FIXME DID I FORGET DIRECTION_IDS IN getStopNames???
    //FIXME SHOULD ALSO TAKE INTO CONSIDERATION DATES WHEN CHECKING WHICH STOPS ARE AVAILABLE
    @Query("SELECT DISTINCT stop_name FROM StopTimes " +
            "JOIN Stops on Stops.stop_id = StopTimes.stop_id " +
            "JOIN Trips ON Trips.trip_id = StopTimes.trip_id " +
            "WHERE trip_headsign = (:headsign) ORDER BY stop_seq")
    suspend fun getStopNames(headsign : String) : List<String>

    @Query("SELECT DISTINCT stop_name FROM StopTimes " +
            "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips on StopTimes.trip_id = Trips.trip_id " +
            "WHERE route_id = (:routeId) AND trips.trip_id LIKE 'trains%' " +
            "AND direction_id = (:directionId) ORDER BY stop_seq;")
    suspend fun getTrainStopNames(routeId : Int, directionId : Int) : List<String>

    @Query("SELECT DISTINCT arrival_time FROM StopTimes " +
            "JOIN  Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips ON StopTimes.trip_id = Trips.trip_id " +
            "JOIN Calendar ON Calendar.service_id = Trips.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND trip_headsign = (:headsign) " +
            "ORDER BY arrival_time")
    //perhaps also use the agency as a search argument
    suspend fun getStopTimes(stopName : String, day : String, headsign: String) : List<Time>

    @Query("SELECT DISTINCT arrival_time FROM StopTimes " +
            "JOIN  Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips ON StopTimes.trip_id = Trips.trip_id " +
            "JOIN Calendar ON Calendar.service_id = Trips.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:time) AND trip_headsign = (:headsign) " +
            "ORDER BY arrival_time")
    //perhaps also use the agency as a search argument
    suspend fun getStopTimes(stopName : String, day : String, time : String, headsign: String) : List<Time>

    @Query("SELECT arrival_time FROM StopTimes " +
            "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips ON Stoptimes.trip_id = Trips.trip_id " +
            "JOIN Calendar ON Calendar.service_id = Trips.service_id " +
            "WHERE route_id = (:routeId) AND Trips.trip_id LIKE 'trains%' " +
            "AND direction_id = (:directionId) AND arrival_time >= (:time) " +
            "AND stop_name = (:stopName)" +
            "ORDER BY stoptimes.stop_seq,arrival_time;")
    suspend fun getTrainStopTimes(routeId: Int, stopName: String, directionId: Int, time: String) : List<Time>

    @Query("SELECT MIN(arrival_time) AS arrival_time FROM (SELECT arrival_time FROM StopTimes " +
            "JOIN Stops ON StopTimes.stop_id = Stops.stop_id " +
            "JOIN Trips on StopTimes.trip_id = Trips.trip_id " +
            "JOIN Calendar ON Calendar.service_id = Trips.service_id " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:time) AND trip_headsign = (:headsign))")
    suspend fun getFavouriteStopTime(stopName : String, day : String, time : String, headsign: String) : Time?
}