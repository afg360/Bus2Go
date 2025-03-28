package dev.mainhq.bus2go.data.data_source.local.database.exo.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(primaryKeys = ["service_id", "date"],
	foreignKeys = [
		ForeignKey(Calendar::class, parentColumns = ["service_id"], childColumns = ["service_id"])
	])
data class CalendarDates (
	@ColumnInfo(name="service_id")
	val serviceId: String,
	@ColumnInfo(name="date")
	val date: String,
//can be 1, or 2
	@ColumnInfo(name="exception_type")
	val exceptionType: Int,
)