package dev.mainhq.bus2go.domain.entity

import android.os.Parcelable

abstract class TransitData: Parcelable {
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
}
