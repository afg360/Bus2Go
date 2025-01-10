package dev.mainhq.bus2go.utils

import android.icu.util.Calendar
import android.os.Parcel
import android.os.Parcelable
import dev.mainhq.bus2go.preferences.SerializableTime
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Deprecated("Use a LocalDateTime object instead of using this class")
/** Allows to do operations more easily on time based formats */
class Time(hour : Int, min : Int, sec : Int) : Parcelable {
    private val _hour : Int
    val hour get() = _hour
    private val _min : Int
    val min get() = _min
    private val _sec = sec % 60
    val sec get() = _sec

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    init {
        this._min = (min + this._sec / 60) % 60
        this._hour = (hour + this._min / 60)  % 24
    }

    constructor(localDateTime: LocalDateTime) : this(
        localDateTime.second,
        localDateTime.minute,
        localDateTime.hour
    )

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
        if (this < time && _hour !in 0..3) return null
        else if (this == time) return Time(0,0,0)
        val realHour = if (_hour in 0..3) _hour + 24 else _hour
        val total = realHour * 3600 + this._min * 60 + this._sec - (time._hour * 3600 + time._min * 60 + time._sec)
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
        return if (this._hour < other._hour) -1
        else if (this._hour > other._hour) 1
        else {
            if (this._min < other._min) -1
            else if (this._min > other._min) 1
            else{
                if (this._sec < other._sec) -1
                else if (this._sec > other._sec) 1
                else 0
            }
        }
    }
    

    override fun toString(): String {
        val hour = if (this._hour >= 10) this._hour.toString() else "0" + this._hour.toString()
        val min = if (this._min >= 10) this._min.toString() else "0" + this._min.toString()
        val sec = if (this._sec >= 10) this._sec.toString() else "0" + this._sec.toString()
        return "$hour:$min:$sec"
    }

    fun toSerializableTime() : SerializableTime{
        return SerializableTime(_hour, _min, _sec)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(_hour)
        dest.writeInt(_min)
        dest.writeInt(_sec)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Time){
            val tmp : Time = other
            return this._hour == tmp._hour && this._min == tmp._min && this._sec == this._sec
        }
        return false
    }

    override fun hashCode(): Int {
        return _hour * 3600 + _min * 60 + _sec
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
