package dev.mainhq.bus2go.domain.entity

data class TransitInfo(
	val routeId : String,
	val routeName : String,
	val trainNum : Int?,
	val transitAgency: TransitAgency
)
