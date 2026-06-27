package dev.mainhq.bus2go.data.data_source.local.database.stm.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity()
data class Shapes (
    @PrimaryKey val id : Int,
    @ColumnInfo(name = "shape_id") val shapeId : Int,
    @ColumnInfo(name = "shape_pt_lat") val latitude : Double,
    @ColumnInfo(name = "shape_pt_long") val longitude : Double,
    @ColumnInfo(name = "shape_pt_sequence") val sequence : Int,
    @ColumnInfo(name = "route_pattern_id") val routePatternId : String,
)