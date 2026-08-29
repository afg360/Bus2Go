package dev.mainhq.bus2go.data.data_source.local.database.exo.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CalendarDAO {
    @Query("SELECT MAX(end_date) FROM Calendar")
    fun getExpirationDate() : Flow<LocalDate>
}