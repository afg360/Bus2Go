package dev.mainhq.schedules.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["service_id"], unique = true)])
data class Calendar (
    @PrimaryKey val id : Int,
    @ColumnInfo(name="service_id") val serviceId : String,
    @ColumnInfo(name="m") val m : Int,
    @ColumnInfo(name="t") val t : Int,
    @ColumnInfo(name="w") val w : Int,
    @ColumnInfo(name="y") val y : Int,
    @ColumnInfo(name="f") val f : Int,
    @ColumnInfo(name="s") val s : Int,
    @ColumnInfo(name="d") val d : Int,
    @ColumnInfo(name="start_date") val startDate : Int,
    @ColumnInfo(name="end_date") val endDate : Int
)