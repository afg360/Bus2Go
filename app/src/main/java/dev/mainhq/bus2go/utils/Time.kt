package dev.mainhq.bus2go.utils

import android.icu.util.Calendar
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import dev.mainhq.bus2go.preferences.SerializableTime
import kotlinx.serialization.Serializable

/** Allows to do operations more easily on time based formats */
class Time(hour : Int, min : Int, sec : Int) : Comparable<Time>, Parcelable {
    var hour : Int
    var min : Int
    var sec : Int

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
    /** Format of string must be = HH:MM:SS */
    constructor(time : String) : this(0,0,0) {
        if (time.isNotEmpty()){
            val list : List<String> = time.split(":")
            try {
                this.hour = list[0].toInt() % 24
                this.min = list[1].toInt()
                this.sec = list[2].toInt()
            }
            catch (ie : IndexOutOfBoundsException){
                Log.e("TIME INIT","The input str must be of the form HH:MM:SS")
                throw Exception()
            }
        }
    }

    constructor(calendar : Calendar) : this(0,0,0){
        this.hour = calendar.get(Calendar.HOUR_OF_DAY)
        this.min = calendar.get(Calendar.MINUTE)
        this.sec = calendar.get(Calendar.SECOND)
    }

    constructor(serializableTime: SerializableTime) : this(0,0,0){
        this.hour = serializableTime.hour
        this.min = serializableTime.min
        this.sec = serializableTime.sec
    }

    fun subtract(time : Time) : Time?{
        val rel = this.compareTo(time)
        if (rel == -1) {
            //TODO if the time is between midnight and 3, + 24
            if (hour in 0..3) hour += 24
            else return null
        } //todo unavailable
        else if (rel == 0) return Time(0,0,0)
        val total = this.hour * 3600 + this.min * 60 + this.sec - (time.hour * 3600 + time.min * 60 + time.sec)
        val hour = total / 3600
        val min = (total - hour * 3600) / 60
        return Time(hour, min, total % 60)
    }

    fun timeRemaining() : Time? {
        return this.subtract(Time(Calendar.getInstance()))
    }

    /** -1 : this < other
     *   0 : this == other
     *   1 : this > other */
    override fun compareTo(other : Time): Int {
        return if (this.hour < other.hour) -1
        else if (this.hour > other.hour) 1
        else {
            if (this.min < other.min) -1
            else if (this.min > other.hour) 1
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
        var result = hour
        result = 31 * result + min
        result = 31 * result + sec
        return result
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