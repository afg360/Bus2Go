package dev.mainhq.bus2go.data.data_source.local.database.stm.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(name="StopsInfoIndex", value = ["route_id", "stop_name"])],
    foreignKeys = [
        ForeignKey(entity = Calendar::class, parentColumns = ["service_id"], childColumns = ["service_id"]),
    ])

data class StopsInfo (
    @PrimaryKey val id : Int,
    //todo MAY NEED TO REMOVE THE FOREIGN KEY...?
    @ColumnInfo(name="stop_name") val stopName : String,
    @ColumnInfo(name="route_id") val routeId : Int,
    @ColumnInfo(name="trip_headsign") val tripHeadsign : String,
    //@ColumnInfo(name="days") val days : String,
    /** Represents the Calendar Id */
    @ColumnInfo(name="service_id") val serviceId : String,
    @ColumnInfo(name="arrival_time") val arrivalTime : String,
    @ColumnInfo(name="stop_seq") val stopSeq : Int
)