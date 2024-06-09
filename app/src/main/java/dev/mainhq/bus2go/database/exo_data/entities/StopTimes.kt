package dev.mainhq.bus2go.database.exo_data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(name = "StopTimesIndexStopId", value = ["stop_id"]), Index(name = "StopTimesIndexTripId", value = ["trip_id"])],
    foreignKeys = [
        ForeignKey(entity = Trips::class, parentColumns = ["trip_id"], childColumns = ["trip_id"]),
        ForeignKey(entity = Stops::class, parentColumns = ["stop_id"], childColumns = ["stop_id"])
    ]
)
data class StopTimes (
    @PrimaryKey @ColumnInfo(name="id") val id : Int,
    @ColumnInfo(name="trip_id") val tripId : String,
    @ColumnInfo(name="arrival_time") val arrivalTime : String,
    @ColumnInfo(name="departure_time") val departureTime : String,
    @ColumnInfo(name="stop_id") val stopId : String,
    @ColumnInfo(name="stop_seq") val stopSeq : Int
)