package dev.mainhq.schedules.utils

import android.util.Log

class Time(private var hour : Int, private var min : Int, private var sec : Int) : Comparable<Time>{

    constructor(time : String) : this(0,0,0){
        //format = HH:MM:SS
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

    fun subtract(time : Time) : Time?{
        val rel = this.compareTo(time)
        if (rel == -1) return null //todo unavailable
        else if (rel == 0) return Time(0,0,0)
        val total = this.hour * 3600 + this.min * 60 + this.sec - (time.hour * 3600 + time.min * 60 + time.sec)
        val hour = total / 3600
        val min = (total - hour * 3600) / 60
        return Time(hour, min, total % 60)
    }

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

    override fun equals(other: Any?): Boolean {
        if (other is Time){
            val tmp : Time = other
            return this.hour == tmp.hour && this.min == tmp.min && this.sec == this.sec
        }
        return false
    }
}