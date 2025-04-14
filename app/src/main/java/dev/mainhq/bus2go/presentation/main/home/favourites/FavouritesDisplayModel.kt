package dev.mainhq.bus2go.presentation.main.home.favourites

import dev.mainhq.bus2go.domain.entity.TransitData

data class FavouritesDisplayModel(
	//not ideal to have it non-private, but allows us for the moment to setup the onLongClickListener
	// for the favouritesListElemsAdapter
	val favouriteTransitData: TransitData,
	val directionText: String,
	//tells us if directionText needs to be truncated bcz of its size
	val toTruncate: Boolean,
	val tripHeadsignText: String,
	val stopNameText: String,
	val arrivalTimeText: String,
	//null if the arrivalTimeText displays None left
	val timeRemainingText: String?,
	val dataDisplayColor: Int,
	val isUrgent: Boolean
) {

	companion object{
		const val DIRECTION_STR_LIMIT = 16
	}

	fun isToRemove(toRemove: List<TransitData>): Boolean{
		return toRemove.contains(favouriteTransitData)
	}
}
