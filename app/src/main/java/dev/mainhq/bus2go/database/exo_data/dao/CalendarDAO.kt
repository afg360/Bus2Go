package dev.mainhq.bus2go.database.exo_data.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.bus2go.database.exo_data.entities.Calendar
import java.time.LocalDate

@Dao
interface CalendarDAO {
    @Query("SELECT MAX(end_date) FROM Calendar")
    suspend fun getMaxEndDate() : LocalDate?
}