package dev.mainhq.bus2go.data.data_source.local.datastore

import android.os.Parcelable
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagDto
import kotlinx.serialization.Serializable

@Serializable
abstract class TransitDataDto: Parcelable {
	abstract val position: Int // used to know the position in the favourites screen, may be modified
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
	abstract val tags: List<TagDto>
}
