package dev.mainhq.bus2go.domain.entity

/**
 * Data used in the main activity to show the next transit passing
 **/
data class TransitDataWithTime(
	val favouriteTransitData: TransitData,
	//val selected: Boolean,
	val arrivalTime : Time?
)
