package dev.mainhq.bus2go.data.data_source.local.database.exo.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["stop_id"], unique = true)])
data class Stops (
    @PrimaryKey @ColumnInfo(name="id") val id : Int,
    @ColumnInfo(name="stop_id") val stopId : String,
    @ColumnInfo(name="stop_code") val stopCode : String,
    @ColumnInfo(name="stop_name") val stopName : String,
    @ColumnInfo(name="lat") val latitude : Double,
    @ColumnInfo(name="long") val longitude : Double,
    @ColumnInfo(name="wheelchair") val wheelchairAccessibility : Int
)