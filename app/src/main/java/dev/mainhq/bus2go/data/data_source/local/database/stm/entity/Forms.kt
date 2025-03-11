package dev.mainhq.bus2go.data.data_source.local.database.stm.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["shape_id"], unique = true)])
data class Forms (
    @PrimaryKey val id : Int,
    @ColumnInfo(name="shape_id") val shapeId : Int
)