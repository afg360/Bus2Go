package dev.mainhq.bus2go.database

import androidx.room.TypeConverter
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import net.bytebuddy.asm.Advice.Local
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Date

class Converters {

    /*
    * If we get no arrival time, most likely the day is not in the Calendar, so no worries about
    * dealing with LocalDate...
    */
    @TypeConverter
    fun toTime(str : String?) : Time? {
        return str?.let{ Time.fromString(it) }
    }

    @TypeConverter
    fun fromTime(time : Time?) : String? {
        return time?.toString()
    }

    @TypeConverter
    fun userQuerySearch(query : FuzzyQuery) : String {
        return query.toString()
    }
}