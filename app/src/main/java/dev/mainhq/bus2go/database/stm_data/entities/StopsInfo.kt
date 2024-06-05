package dev.mainhq.bus2go.database.stm_data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(name="StopsInfoIndex", value = ["trip_headsign", "stop_name"])], foreignKeys = [])
data class StopsInfo (
    @PrimaryKey val id : Int,
    //todo MAY NEED TO REMOVE THE FOREIGN KEY...?
    @ColumnInfo(name="stop_name") val stopName : String,
    @ColumnInfo(name="trip_headsign") val tripHeadsign : String,
    @ColumnInfo(name="days") val days : String,
    @ColumnInfo(name="arrival_time") val arrivalTime : String,
    @ColumnInfo(name="stop_seq") val stopSeq : Int
)