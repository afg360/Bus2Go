package dev.mainhq.bus2go.database.stm_data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Forms::class, parentColumns = ["shape_id"], childColumns = ["shape_id"])])
data class Shapes (
    @PrimaryKey val id : Int,
    @ColumnInfo(name = "shape_id") val shapeId : Int,
    @ColumnInfo(name = "lat") val latitude : Double,
    @ColumnInfo(name = "long") val longitude : Double,
    @ColumnInfo(name = "sequence") val sequence : Int,
)