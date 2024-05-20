package dev.mainhq.bus2go.database

import androidx.room.TypeConverter
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time

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