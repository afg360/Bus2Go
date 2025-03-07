package dev.mainhq.bus2go.database.stm_data.dao

import androidx.room.Dao
import androidx.room.Query
import java.time.LocalDate

@Dao
interface CalendarDAO {
    @Query("SELECT MAX(end_date) FROM Calendar")
    suspend fun getMaxEndDate() : LocalDate?
}