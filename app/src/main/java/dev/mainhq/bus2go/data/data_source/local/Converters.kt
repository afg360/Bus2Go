package dev.mainhq.bus2go.data.data_source.local

import androidx.room.TypeConverter
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    /**
     * Only used for CalendarDAOs
     **/
    fun toLocalDate(str: String?): LocalDate?{
        return str?.let{ LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE)}
    }

    @TypeConverter
    fun userQuerySearch(query : FuzzyQuery) : String {
        return query.toString()
    }
}