package dev.mainhq.bus2go.domain.entity

import dev.mainhq.bus2go.utils.Time

data class FavouriteTransitInfo(
	val transitData: TransitData,
	val arrivalTime : Time?,
	val agency : TransitAgency
)
