package dev.mainhq.schedules.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverter
import dev.mainhq.schedules.utils.Time

@Dao
interface StopsInfoDAO {

    @Query("SELECT DISTINCT stop_name FROM StopsInfo " +
            "WHERE trip_headsign = (:headsign) ORDER BY stop_seq")
    suspend fun getStopNames(headsign : String) : List<String>

    //TODO check if i really need trip_headsign
    @Query("SELECT DISTINCT arrival_time FROM StopsInfo " +
            "WHERE stop_name = (:stopName) AND days LIKE '%' || (:day) || '%' " +
            "AND arrival_time >= (:time) AND trip_headsign = (:headsign) " +
            "ORDER BY arrival_time")
    suspend fun getStopTimes(stopName : String, day : String, time : String, headsign: String) : List<Time>
}

