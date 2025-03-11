package dev.mainhq.bus2go.data.data_source.local.database.stm.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(indices = [])
data class Stops (
    @PrimaryKey @ColumnInfo(name="id") val id : Int,
    @ColumnInfo(name="stop_id") val stopId : String,
    @ColumnInfo(name="stop_code") val stopCode : Int,
    @ColumnInfo(name="stop_name") val stopName : String,
    @ColumnInfo(name="lat") val latitude : Double,
    @ColumnInfo(name="long") val longitude : Double,
    @ColumnInfo(name="wheelchair") val wheelchair : Int
)