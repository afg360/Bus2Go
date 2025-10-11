package dev.mainhq.bus2go.data.data_source.local.datastore.deprecated

import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
@Deprecated("User v3")
abstract class TransitDataDto_v2: Parcelable {
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
}