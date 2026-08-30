package dev.mainhq.bus2go.domain.entity

import android.os.Parcel
import android.os.Parcelable
import dev.mainhq.bus2go.utils.toEpochDay
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Allows to do operations more easily on time based formats */
class Time(private val localDateTime: LocalDateTime) : Parcelable, Comparable<Time> {

    constructor(localDate: LocalDate, localTime: LocalTime) : this(
        LocalDateTime.of(localDate, localTime)
    )

    constructor(parcel: Parcel) : this(
        LocalDate.of(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
        ),
        LocalTime.of(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
        )
    )

    /**
     * Create a Time object based on today (deals with 3am thingy)
     **/
    constructor(localTime: LocalTime) : this(
        LocalDateTime.of(LocalDate.now(), localTime)
    )

    constructor(localDate: LocalDate) : this(
        LocalDateTime.of(localDate, LocalTime.now())
    )

    constructor(years: Int, months: Int, days: Int, hours: Int, mins: Int, secs: Int): this(
        LocalDateTime.of(years, months, days, hours, mins, secs)
    )

    /**
     * We do not expect Durations of more than a day
     * @return If the duration is negative, null.
     **/
    operator fun minus(time : Time): Duration? {
        //Takes the sec arg first...
        if (this < time) return null
        return Duration.between(time.localDateTime, localDateTime)
//        //duration.seconds === all the time (hours + minutes) in seconds
//        var mins = duration.seconds / 60
//        val secs = duration.seconds - mins * 60
//        val hours = (mins / 60) % 24 //FIXME added the % 24 as a hack
//        mins -= 60 * hours
//        return LocalTime.of(hours.toInt(), mins.toInt(), secs.toInt())
    }

    /** @return null if this < time. */
    fun minusDays(time: Time): Day? {
        if (this < time) return null
        val duration = Duration.between(time.localDateTime, localDateTime)
        //duration.seconds === all the time (hours + minutes) in seconds
        val mins = duration.toMinutes() % 60
        val secs = duration.seconds % 60
        val hours = duration.toHours() % 24
        val days = duration.toDays()
        return Day(LocalTime.of(hours.toInt(), mins.toInt(), secs.toInt()), days)
    }

    data class Day (val time: LocalTime, val days: Long){
        operator fun compareTo(other: Day): Int {
            val diff = this.days - other.days
            return if (diff != 0L) (this.days - diff).toInt()
            else this.time.compareTo(other.time)
        }
    }

    fun timeRemaining(): Duration? {
        return this - Time(LocalDateTime.now())
    }

    override operator fun compareTo(other : Time): Int {
        return localDateTime.compareTo(other.localDateTime)
    }

    /**
     * Get a string representing the hour, minute and secs.
     * @return Format: HH:MM:SS ([DateTimeFormatter.ISO_TIME])
     **/
    fun getTimeString(): String {
        //ignore nanosecs
        return localDateTime.format(DateTimeFormatter.ISO_TIME).split(".")[0]
    }

    /**
     * Get a string representing the year, month and day.
     * @return Format example: Aug. 01, 2023
     **/
    fun getDateOfYearString(): String {
        //ignore nanosecs
        return localDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }

    /**
     * Get a string represented by [dateTimeFormatter].
     **/
    private fun format(dateTimeFormatter: DateTimeFormatter): String {
        //ignore nanosecs
        return localDateTime.format(dateTimeFormatter)
    }

    /**
     * Get a string representing the year, month and day.
     * @return Format: YYYYMMDD ([DateTimeFormatter.BASIC_ISO_DATE])
     **/
    fun getTodayString(): String {
        return localDateTime.format(DateTimeFormatter.BASIC_ISO_DATE)
    }

    /**
     * Get the 1 letter representation of the day in the week of this Time object.
     **/
    fun getDayString(): String {
        return when (localDateTime.dayOfWeek) {
            DayOfWeek.SUNDAY -> "d"
            DayOfWeek.MONDAY -> "m"
            DayOfWeek.TUESDAY -> "t"
            DayOfWeek.WEDNESDAY -> "w"
            DayOfWeek.THURSDAY -> "y"
            DayOfWeek.FRIDAY -> "f"
            DayOfWeek.SATURDAY -> "s"
            else -> throw IllegalStateException("Cannot have a non day of the week!")
        }
    }

    override fun toString(): String {
        return "${localDateTime.toLocalDate()}, ${localDateTime.toLocalTime()}"
    }

    fun resetTime(): Time {
        return Time(localDateTime.toLocalDate(), LocalTime.of(4, 0, 0))
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(localDateTime.year)
        parcel.writeInt(localDateTime.monthValue)
        parcel.writeInt(localDateTime.dayOfMonth)
        parcel.writeInt(localDateTime.hour)
        parcel.writeInt(localDateTime.minute)
        parcel.writeInt(localDateTime.second)
    }


    override fun equals(other: Any?): Boolean {
        if (other is Time){
            val tmp : Time = other
            return localDateTime == tmp.localDateTime
        }
        return false
    }

    override fun hashCode(): Int {
        return localDateTime.hashCode() + this.localDateTime.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<Time> {
        override fun createFromParcel(parcel: Parcel): Time {
            return Time(parcel)
        }

        override fun newArray(size: Int): Array<Time?> {
            return arrayOfNulls(size)
        }

        /**
         * Create a new time object from a String. Used inside Room Data Types converters
         * @throws java.time.format.DateTimeParseException
         **/
        //TODO only in strings we need to consider when time is greater than 24h since only in the strings it happens...
        fun fromString(time : String, date : String): Time {
            val localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)
            val list : List<String> = time.split(":")
            try {
                return if (list[0].toInt() >= 24){
                    return Time(localDate.plusDays(1), LocalTime.of(list[0].toInt() % 24, list[1].toInt(), list[2].toInt()))
                }
                else Time(localDate, LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME))
            }
            catch (ie : IndexOutOfBoundsException){
                throw IllegalArgumentException("The input str must be of the form HH:MM:SS")
            }
        }

        /**
         * Creates a new Time object from some LocalTime string. The LocalDate will be set to now.
         * @param time Must be of format HH:MM:SS
         * @throws IllegalArgumentException
         **/
        fun fromString(time : String): Time {
            val localDate = LocalDate.now()
            val list : List<String> = time.split(":")
            try {
                return if (list[0].toInt() >= 24){
                    Time(localDate.plusDays(1), LocalTime.of(list[0].toInt() % 24, list[1].toInt(), list[2].toInt()))
                }
                else Time(localDate, LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME))
            }
            catch (ie : IndexOutOfBoundsException){
                throw IllegalArgumentException("The input str must be of the form HH:MM:SS")
            }
        }

        fun now(): Time = Time(LocalDateTime.now())

        fun fromUnix(unixTime : Long) : Time {
            //get Canada timeZone which is UTC - 5
            return Time(LocalDateTime.ofEpochSecond(unixTime, 0, ZoneOffset.ofHours(-5)))
        }

        fun fromMillis(millis : Long) : Time {
            //get Canada timeZone which is UTC - 5
            return Time(LocalDateTime.ofEpochSecond(millis / 1000, 0, ZoneOffset.ofHours(0)))
        }

    }
}
