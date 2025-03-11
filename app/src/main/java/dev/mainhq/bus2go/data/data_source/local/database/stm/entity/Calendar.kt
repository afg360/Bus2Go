package dev.mainhq.bus2go.data.data_source.local.database.stm.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity//(indices = [Index(value = ["service_id"], unique = true)])
data class Calendar (
    @PrimaryKey //val id : Int,
    @ColumnInfo(name="service_id") val serviceId : String,
    @ColumnInfo(name="days") val days : String,
    @ColumnInfo(name="start_date") val startDate : Int,
    @ColumnInfo(name="end_date") val endDate : Int
)