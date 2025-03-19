package dev.mainhq.bus2go.domain.entity

import android.os.Parcelable

/** Entity to deal to interface with data coming from dataStore */
abstract class FavouriteTransitData: Parcelable {
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String


	override fun describeContents(): Int {
		return 0
	}
}
