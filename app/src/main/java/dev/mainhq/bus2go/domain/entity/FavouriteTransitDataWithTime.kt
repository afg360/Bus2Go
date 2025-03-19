package dev.mainhq.bus2go.domain.entity

import dev.mainhq.bus2go.utils.Time

data class FavouriteTransitDataWithTime(
	val favouriteTransitData: FavouriteTransitData,
	val arrivalTime : Time?
)
