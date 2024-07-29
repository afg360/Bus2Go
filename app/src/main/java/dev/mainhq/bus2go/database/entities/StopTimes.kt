package dev.mainhq.bus2go.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*@Entity(indices = [Index(name = "StopTimesIndex", value = ["stop_id", "trip_id"])],
    foreignKeys = [ForeignKey(entity = Trips::class, parentColumns = ["trip_id"], childColumns = ["trip_id"]),
        ForeignKey(entity = Stops::class, parentColumns = ["stop_id"], childColumns = ["stop_id"])
    ]
)
data class StopTimes (
    @PrimaryKey @ColumnInfo(name="id") val id : Int,
    @ColumnInfo(name="trip_id") val tripId : Int,
    @ColumnInfo(name="arrival_time") val arrivalTime : String,
    @ColumnInfo(name="departure_time") val departureTime : String,
    @ColumnInfo(name="stop_id") val stopId : Int,
    @ColumnInfo(name="stop_seq") val stopSeq : Int
)*/