package dev.mainhq.schedules.database.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.schedules.database.entities.Stops

@Dao
interface StopsDAO {
    //TODO THIS QUERY IS A MEMORY HOG!
    @Query("SELECT DISTINCT * FROM Stops JOIN StopTimes ON " +
            "Stops.stop_id = StopTimes.stop_id LIMIT 10;")
    suspend fun getAllStopNames() : List<Stops>
}