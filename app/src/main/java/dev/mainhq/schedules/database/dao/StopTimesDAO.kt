package dev.mainhq.schedules.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
interface StopTimesDAO {
    @Query("SELECT DISTINCT arrival_time FROM StopTimes JOIN Trips ON StopTimes.trip_id = Trips.trip_id " +
            "WHERE Trips.trip_headsign LIKE (:headsign) ORDER BY arrival_time;")
    suspend fun getAllArrivalTimesFromBusNum(headsign : String) : List<String>
    //check if we can make better and more efficient queries, or if i should use indexes
    @Query("SELECT DISTINCT stop_name AS stopName,stop_code AS stopCode " +
            "FROM StopTimes AS st JOIN Trips ON st.trip_id = Trips.trip_id " +
            "JOIN Stops AS s ON s.stop_id = st.stop_id " +
            "WHERE Trips.trip_headsign = (:headsign) ORDER BY st.stop_seq;")
    suspend fun getArrivalTimesFromBusNumNow(headsign: String) : List<StopInfo>
}

data class StopInfo(val stopName : String, val stopCode : Int)
