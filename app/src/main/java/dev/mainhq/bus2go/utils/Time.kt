package dev.mainhq.bus2go.utils

import android.os.Parcel
import android.os.Parcelable
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Allows to do operations more easily on time based formats */
class Time(localDateTime: LocalDateTime) : Parcelable {

    private val localTime = localDateTime.toLocalTime()
    private val localDate = localDateTime.toLocalDate()

    constructor(parcel: Parcel) : this(
        LocalDateTime.of(
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
    )

    constructor(localDate: LocalDate, localTime: LocalTime) : this(
        LocalDateTime.of(localDate, localTime)
    )

    /**
     * Create a Time object based on today (deals with 3am thingy)
     **/
    constructor(localTime: LocalTime) : this(
        LocalDateTime.of(LocalDate.now(), localTime)
    )

    /**
     * We do not expect Durations of more than a day
     * @return If the duration is negative, null.
     **/
    operator fun minus(time : Time) : LocalTime?{
        //Takes the sec arg first...
        if (this < time) return null
        val duration = Duration.between(LocalDateTime.of(time.localDate, time.localTime), LocalDateTime.of(this.localDate, this.localTime))
        //duration.seconds === all the time (hours + minutes) in seconds
        var mins = duration.seconds / 60
        val secs = duration.seconds - mins * 60
        val hours = mins / 60
        mins -= 60 * hours
        return if (duration.isNegative) null
               else LocalTime.of(hours.toInt(), mins.toInt(), secs.toInt())
    }

    fun timeRemaining() : LocalTime? {
        return this - Time(LocalDateTime.now())
    }

    operator fun compareTo(other : Time): Int {
        return LocalDateTime.of(this.localDate, this.localTime)
            .compareTo(LocalDateTime.of(other.localDate, other.localTime))
    }

    /**
     * Get a string representing the hour, minute and secs
     * @return Format: HH:MM:SS (DateTimeFormatter.ISO_TIME)
     * */
    fun getTimeString(): String{
        //ignore nanosecs
        return this.localTime.format(DateTimeFormatter.ISO_TIME).split(".")[0]
    }

    /**
     * Get a string representing the year, month and day.
     * @return Format: YYYYMMDD
     **/
    fun getTodayString(): String {
        return this.localDate.format(DateTimeFormatter.BASIC_ISO_DATE)
    }

    /**
     * Get the 1 letter representation of the day in the week of this Time object.
     **/
    fun getDayString(): String {
        return when (this.localDate.dayOfWeek) {
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
        return "${this.localDate}, ${this.localTime}"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(localDate.year)
        parcel.writeInt(localDate.monthValue)
        parcel.writeInt(localDate.dayOfMonth)
        parcel.writeInt(localTime.hour)
        parcel.writeInt(localTime.minute)
        parcel.writeInt(localTime.second)
    }


    override fun equals(other: Any?): Boolean {
        if (other is Time){
            val tmp : Time = other
            return this.localDate == tmp.localDate && this.localTime == tmp.localTime
        }
        return false
    }

    override fun hashCode(): Int {
        return this.localDate.hashCode() + this.localTime.hashCode()
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
         * @throws DateTimeParseException
         **/
        //TODO only in strings we need to consider when time is greater than 24h since only in the strings it happens...
        fun fromString(time : String, date : String) : Time {
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
        fun fromString(time : String) : Time {
            val localDate = LocalDate.now()
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

        fun now(): Time = Time(LocalDateTime.now())

        fun fromUnix(unixTime : Long) : Time{
            //get Canada timeZone which is UTC - 5
            return Time(LocalDateTime.ofEpochSecond(unixTime, 0, ZoneOffset.ofHours(-5)))
        }
    }
}
