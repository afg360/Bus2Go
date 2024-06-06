package dev.mainhq.bus2go.database.exo_data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index
import androidx.room.PrimaryKey;

@Entity(indices = [Index(value = ["route_id"], unique = true)])
data class Routes (
    @PrimaryKey @ColumnInfo(name="id") val id : Int,
    @ColumnInfo(name="route_id") val routeId : String,
    @ColumnInfo(name="route_long_name") val routeLongName : String,
    @ColumnInfo(name="route_type") val routeType : Int,
    @ColumnInfo(name="route_color") val routeColor : String,
    @ColumnInfo(name="route_text_color") val routeTextColor : String
)