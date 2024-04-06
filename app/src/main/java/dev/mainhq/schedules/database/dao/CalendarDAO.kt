package dev.mainhq.schedules.database.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.schedules.database.entities.Calendar

@Dao
interface CalendarDAO {
    @Query("SELECT * FROm Calendar;")
    suspend fun getAllCalendarInfo() : List<Calendar>
}