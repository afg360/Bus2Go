package app.bus2go.android.domain.entity

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Allows to do operations more easily on time based formats */
class Time(private val localDateTime: LocalDateTime) : Comparable<Time> {

    //Only used for when we query databases
    private val isAfterMidnight = localDateTime.hour < 4

	private constructor(localDate: LocalDate, localTime: LocalTime) : this(
        LocalDateTime.of(localDate, localTime)
    )

    /**
     * We do not expect Durations of more than a day
     * @return If the duration is negative, null.
     **/
    operator fun minus(time : Time): Duration? {
        //Takes the sec arg first...
        val time2 = if (this.isAfterMidnight) this.localDateTime.plusDays(1)
        else this.localDateTime

        val time1 = if (time.isAfterMidnight) time.localDateTime.plusDays(1)
        else time.localDateTime

        return if (time2 < time1) null
        //first arg is start (meaning that in a foo - bar, it would be bar), and second 1 is end
        else Duration.between(time1, time2)
    }

    fun timeRemaining(): Duration? {
        return this - Time(LocalDateTime.now())
    }

    override operator fun compareTo(other : Time): Int {
        return localDateTime.compareTo(other.localDateTime)
    }

    /**
     * Get a string representing the hour, minute and secs. Only to be used when querying databases.
     * @return Format: HH:MM:SS ([DateTimeFormatter.ISO_TIME])
     **/
    fun getQueryTimeString(): String {
        return if (isAfterMidnight) {
            "${localDateTime.hour + 24}:${getTimeString().substringAfter(":")}"
        }
        else getTimeString()
    }

    /**
     * Get a string representing the hour, minute and secs.
     * @return Format: HH:MM:SS ([DateTimeFormatter.ISO_TIME])
     **/
    fun getTimeString(): String {
        return localDateTime.format(DateTimeFormatter.ISO_TIME).split(".")[0]
    }

    /**
     * Get a string representing the year, month and day.
     * @return Format example: Aug. 01, 2023
     **/
    fun getDateOfYearString(): String {
        //ignore nanosecs
        return if (isAfterMidnight) {
            localDateTime.plusDays(1).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
        else localDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }


    /**
     * Get a string representing the year, month and day.
     * @return Format: YYYYMMDD ([DateTimeFormatter.BASIC_ISO_DATE])
     **/
    fun getTodayString(): String {
        return if (isAfterMidnight) {
            localDateTime.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)
        }
        else localDateTime.format(DateTimeFormatter.BASIC_ISO_DATE)
    }

    /**
     * Get a string representing the year, month and day. Only to be used when querying databases
     * @return Format: YYYYMMDD ([DateTimeFormatter.BASIC_ISO_DATE])
     **/
    fun getQueryTodayString(): String {
        return localDateTime.format(DateTimeFormatter.BASIC_ISO_DATE)
    }

    /**
     * Get the 1 letter representation of the day in the week of this Time object.
     **/
    fun getQueryDayString(): String {
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
        return localDateTime.toString()
    }

    fun resetTime(): Time {
        return Time(localDateTime.toLocalDate(), LocalTime.of(4, 0, 0))
    }

    override fun equals(other: Any?): Boolean {
        if (other is Time){
            val tmp : Time = other
            return localDateTime == tmp.localDateTime && isAfterMidnight == tmp.isAfterMidnight
        }
        return false
    }

    override fun hashCode(): Int {
        return localDateTime.hashCode()
    }

    companion object {

        /**
         * Creates a new Time object from some [LocalTime] string. The date will be set to today.
         * @param time Must be of format HH:MM:SS
         * @throws IllegalArgumentException if wrong input format for [time].
         **/
        fun fromTimeString(time : String): Time {
            val localDate = LocalDate.now()
            val list : List<String> = time.split(":")
            try {
                return if (list[0].toInt() > 23){
                    Time(localDate, LocalTime.of(list[0].toInt() % 24, list[1].toInt(), list[2].toInt()))
                }
                else if (list[0].toInt() < 4) {
                    Time(localDate.minusDays(1), LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME))
                }
                else Time(localDate, LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME))
            }
            catch (ie : IndexOutOfBoundsException){
                throw IllegalArgumentException("The input str must be of the form HH:MM:SS")
            }
        }


        fun now(): Time {
            val dateTime = LocalDateTime.now()
            return if (dateTime.hour < 4) Time(dateTime.minusDays(1))
            else Time(dateTime)
        }

        fun fromMillis(millis : Long) : Time {
            val dateTime = LocalDateTime.ofEpochSecond(millis / 1000, 0, ZoneOffset.ofHours(0))
            return if (dateTime.hour < 4) {
                Time(dateTime.minusDays(1))
            }
            else Time(dateTime)
        }

    }
}
