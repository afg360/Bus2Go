package dev.mainhq.bus2go.utils

import android.icu.util.Calendar
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import dev.mainhq.bus2go.preferences.SerializableTime
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Allows to do operations more easily on time based formats */
class Time(hour : Int, min : Int, sec : Int) : Parcelable {
    val hour : Int
    val min : Int
    val sec : Int

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    init {
        this.sec = sec % 60
        this.min = (min + this.sec / 60) % 60
        this.hour = (hour + this.min / 60)  % 24
    }

    constructor(calendar: Calendar) : this(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )

    constructor(serializableTime: SerializableTime) : this(
            serializableTime.hour,
            serializableTime.min,
            serializableTime.sec
        )


    object TimeBuilder{
        /** Format of string must be = HH:MM:SS */
        fun fromString(time : String) : Time{
            val list : List<String> = time.split(":")
            try {
                return Time(
                    hour = list[0].toInt() % 24,
                    min = list[1].toInt(),
                    sec = list[2].toInt()
                )
            }
            catch (ie : IndexOutOfBoundsException){
                throw IllegalArgumentException("The input str must be of the form HH:MM:SS")
            }
        }

        fun fromUnix(unixTime : Long) : Time{
            val date = Date(unixTime * 1000)

            // Format the Date object using SimpleDateFormat, now for Canada
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA)
            sdf.timeZone = TimeZone.getDefault()

            val formattedTime = sdf.format(date)
            return fromString(formattedTime.split(" ", limit = 2)[1])

        }
    }


    /** Returns null if this < time and this > 3, may need to allow negative times? */
    operator fun minus(time : Time) : Time?{
        if (this < time && hour !in 0..3) return null
        else if (this == time) return Time(0,0,0)
        val realHour = if (hour in 0..3) hour + 24 else hour
        val total = realHour * 3600 + this.min * 60 + this.sec - (time.hour * 3600 + time.min * 60 + time.sec)
        val hour = total / 3600
        val min = (total - hour * 3600) / 60
        return Time(hour, min, total % 60)
    }

    fun timeRemaining() : Time? {
        return this - Time(Calendar.getInstance())
    }

    /** -1 : this < other,
     *   0 : this == other,
     *   1 : this > other */
    operator fun compareTo(other : Time): Int {
        return if (this.hour < other.hour) -1
        else if (this.hour > other.hour) 1
        else {
            if (this.min < other.min) -1
            else if (this.min > other.min) 1
            else{
                if (this.sec < other.sec) -1
                else if (this.sec > other.sec) 1
                else 0
            }
        }
    }
    

    override fun toString(): String {
        val hour = if (this.hour >= 10) this.hour.toString() else "0" + this.hour.toString()
        val min = if (this.min >= 10) this.min.toString() else "0" + this.min.toString()
        val sec = if (this.sec >= 10) this.sec.toString() else "0" + this.sec.toString()
        return "$hour:$min:$sec"
    }

    fun toSerializableTime() : SerializableTime{
        return SerializableTime(hour, min, sec)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(hour)
        dest.writeInt(min)
        dest.writeInt(sec)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Time){
            val tmp : Time = other
            return this.hour == tmp.hour && this.min == tmp.min && this.sec == this.sec
        }
        return false
    }

    override fun hashCode(): Int {
        return hour * 3600 + min * 60 + sec
    }

    companion object CREATOR : Parcelable.Creator<Time> {
        override fun createFromParcel(parcel: Parcel): Time {
            return Time(parcel)
        }

        override fun newArray(size: Int): Array<Time?> {
            return arrayOfNulls(size)
        }
    }
}
