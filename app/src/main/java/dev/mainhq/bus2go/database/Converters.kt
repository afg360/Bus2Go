package dev.mainhq.bus2go.database

import androidx.room.TypeConverter
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import net.bytebuddy.asm.Advice.Local
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Date

class Converters {

    @TypeConverter
    fun toLocalDateTime(str: String?) : LocalTime? {
        return str?.let{
            val processedStr = it.split(":").toMutableList()
            if (processedStr[0].toInt() > 23){
                //FIXME must write HH:mm:ss, even if we get back to 0...
                processedStr[0] = "0${(processedStr[0].toInt() % 24)}"
            }
            LocalTime.parse(
                processedStr.reduce{ str1, str2 -> "$str1:$str2" },
                DateTimeFormatter.ofPattern("kk:mm:ss")
            )
        }
    }

    @TypeConverter
    @Deprecated("Use toLocalDateTime instead")
    fun toTime(str : String?) : Time? {
        return str?.let{ Time.TimeBuilder.fromString(it) }
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