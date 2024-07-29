package dev.mainhq.bus2go.database.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.bus2go.database.entities.Calendar

@Dao
interface CalendarDAO {
    @Query("SELECT * FROM Calendar;")
    suspend fun getAllCalendarInfo() : List<Calendar>
}