package app.bus2go.android.domain.entity

/**
 * Data used in the main activity to show the next transit passing
 **/
data class FavouriteTransitDataWithTime(
	val favouriteTransitData: FavouriteTransitData,
	//val selected: Boolean,
	val arrivalTime : Time?
)
