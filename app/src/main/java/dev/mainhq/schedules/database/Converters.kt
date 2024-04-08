package dev.mainhq.schedules.database

import androidx.room.TypeConverter
import dev.mainhq.schedules.utils.FuzzyQuery
import dev.mainhq.schedules.utils.Time

class Converters {
    @TypeConverter
    fun toTime(str : String?) : Time? {
        return str?.let{ Time(it) }
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