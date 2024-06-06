package dev.mainhq.bus2go.database.exo_data.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.bus2go.database.exo_data.entities.Calendar

@Dao
interface CalendarDAO {
    @Query("SELECT * FROM Calendar;")
    suspend fun getAllCalendarInfo() : List<Calendar>
}