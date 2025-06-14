package dev.mainhq.bus2go.data.data_source.local.database.exo.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey;

@Entity (
    indices = [
        Index(name = "TripsIndexTripId", value = ["trip_id"]),
        Index(name = "test", value = ["trip_headsign"]),
        Index(value = ["trip_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(entity = Routes::class, parentColumns = ["route_id"], childColumns = ["route_id"]),
        ForeignKey(entity = Calendar::class, parentColumns = ["service_id"], childColumns = ["service_id"]),
        ForeignKey(entity = Forms::class, parentColumns = ["shape_id"], childColumns = ["shape_id"])
    ]
)
data class Trips(
    @PrimaryKey val id : Int,
    @ColumnInfo(name="trip_id") val tripId : String,
    @ColumnInfo(name="route_id") val routeId : String,
    @ColumnInfo(name="service_id") val serviceId : String,
    @ColumnInfo(name="trip_headsign") val tripHeadsign : String,
    @ColumnInfo(name="direction_id") val directionId : Int,
    @ColumnInfo(name="shape_id") val shapeId : String,
    @ColumnInfo(name="wheelchair") val wheelChairAccessibility : Int
)
