package dev.mainhq.bus2go.data.data_source.local.datastore

import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
abstract class TransitDataDto: Parcelable {
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
}
