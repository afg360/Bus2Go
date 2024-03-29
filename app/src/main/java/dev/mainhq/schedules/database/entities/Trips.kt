package dev.mainhq.schedules.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//trip column values are decale de 1
@Entity
data class Trips(
    @PrimaryKey val id : Int,

    @ColumnInfo(name="tripid") val tripId : Int,

    @ColumnInfo(name="routeid") val routeId : String,

    @ColumnInfo(name="serviceid") val serviceId : Int,

    @ColumnInfo(name="trip_headsign") val tripHeadsign : String,

    @ColumnInfo(name="direction_id") val directionId : Int,

    @ColumnInfo(name="shape_id") val shapeId : Int,

    @ColumnInfo(name="wheelchair_accessible") val wheelChairAccessibility : Int
)
