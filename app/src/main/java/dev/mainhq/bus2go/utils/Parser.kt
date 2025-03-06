package dev.mainhq.bus2go.utils

import android.icu.util.Calendar
import java.time.LocalDateTime
import java.util.Locale

//right now only for the main activities
//todo may use db operations instead


/** Deals with french characters **/
fun toParsable(txt: String): String {
    var str = txt
    str = str.lowercase(Locale.getDefault())
        .replace("'", "")
        .replace("-", "")
        .replace(" ", "")
        .replace("/", "")
        .replace("é", "e")
        .replace("è", "e")
        .replace("ê", "e")
        .replace("ç", "c")
        .replace("î", "i")
        .replace("ô", "o")
        .replace("û", "u")
    return str
}


@Deprecated("Use a LocalDateTime instead of a Calendar object",
    replaceWith = ReplaceWith("getDayString(calendar : LocalDateTime) : String"))
fun getDayString(calendar : Calendar) : String{
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "d"
        Calendar.MONDAY -> "m"
        Calendar.TUESDAY -> "t"
        Calendar.WEDNESDAY -> "w"
        Calendar.THURSDAY -> "y"
        Calendar.FRIDAY -> "f"
        Calendar.SATURDAY -> "s"
        else -> throw IllegalStateException("Cannot have a non day of the week!")
    }
}


data class TransitInfo(val routeId : String, val routeName : String, val trainNum : Int?, val transitAgency: TransitAgency)

enum class TransitAgency : java.io.Serializable{
    STM, EXO_TRAIN, EXO_OTHER
}

