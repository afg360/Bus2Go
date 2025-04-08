package dev.mainhq.bus2go.presentation.main.home.favourites

import dev.mainhq.bus2go.domain.entity.TransitDataWithTime

/** A helper class that let's us know if a favouriteTransitDataWithTime is selected */
data class FavouriteTransitDataWithTimeAndSelection(
	val transitDataWithTime: TransitDataWithTime,
	var isSelected: Boolean
)
