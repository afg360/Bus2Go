package dev.mainhq.bus2go.data.data_source.local.database.stm.dao

import androidx.room.Dao
import androidx.room.Query
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.CalendarDates

@Dao
/** For testing purposes */
interface CalendarDatesDAO {
	@Query("SELECT * FROM CalendarDates")
	suspend fun getAllCalendarDates(): List<CalendarDates>

	@Query("SELECT * FROM CalendarDates WHERE date = (:date)")
	/** Format YYYYMMDD */
	suspend fun getCalendarDateException(date: String): List<CalendarDates>
}