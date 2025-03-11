package dev.mainhq.bus2go.data.data_source.local.database.stm.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface StopsDAO {
    @Query("SELECT DISTINCT stop_name FROM stops where stop_id = (:stopId);")
    suspend fun getStopName(stopId : Int) : String
}