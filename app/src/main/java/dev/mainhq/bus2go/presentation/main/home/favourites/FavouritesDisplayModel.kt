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
	//null if the timeRemainingText displays None left
	val arrivalTimeText: String?,
	val timeRemainingText: String,
	val dataDisplayColor: Int,
	val urgency: Urgency
) {

	companion object{
		const val DIRECTION_STR_LIMIT = 16
	}

	/**
	 * Returns true if the basic data is the same. Used to determine whether the time has changed only
	 * or the whole data */
	fun isMainDataEqual(other: FavouritesDisplayModel): Boolean {
		return favouriteTransitData == other.favouriteTransitData &&
				directionText == other.directionText && tripHeadsignText == other.tripHeadsignText &&
				stopNameText == other.stopNameText
	}

	fun isToRemove(toRemove: List<TransitData>): Boolean{
		return toRemove.contains(favouriteTransitData)
	}
}
