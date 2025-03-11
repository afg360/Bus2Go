package dev.mainhq.bus2go.domain.entity.stm

data class CalendarDates (
	val serviceId: String,
	val date: String,
	//can be 1, or 2
	val exceptionType: Int,
)
