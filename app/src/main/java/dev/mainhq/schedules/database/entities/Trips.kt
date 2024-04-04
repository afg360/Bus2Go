package dev.mainhq.schedules.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Index
import androidx.room.PrimaryKey;

//trip column values are decale de 1
@Entity (indices = [Index(name="TripsIndex", value = ["tripid"])])
data class Trips(
    @PrimaryKey val id : Int,

    @ColumnInfo(name="tripid") val tripId : Int,

    @ColumnInfo(name="routeid") val routeId : Int,

    @ColumnInfo(name="serviceid") val serviceId : String,

    @ColumnInfo(name="trip_headsign") val tripHeadsign : String,

    @ColumnInfo(name="direction_id") val directionId : Int,

    @ColumnInfo(name="shape_id") val shapeId : Int,

    @ColumnInfo(name="wheelchair_accessible") val wheelChairAccessibility : Int
)
