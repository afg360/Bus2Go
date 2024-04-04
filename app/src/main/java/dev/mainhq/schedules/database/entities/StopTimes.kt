package dev.mainhq.schedules.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(name="StopTimesIndex", value=["tripid"])])
data class StopTimes (
    @PrimaryKey @ColumnInfo(name="id") val id : Int,
    @ColumnInfo(name="tripid") val tripId : Int,
    @ColumnInfo(name="arrivaltime") val arrivalTime : String,
    @ColumnInfo(name="departuretime") val departureTime : String,
    @ColumnInfo(name="stopid") val stopId : Int,
    @ColumnInfo(name="stopseq") val stopSeq : Int
)