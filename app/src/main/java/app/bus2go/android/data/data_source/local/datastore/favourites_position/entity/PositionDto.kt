package app.bus2go.android.data.data_source.local.datastore.favourites_position.entity

import android.annotation.SuppressLint
import app.bus2go.android.domain.entity.TransitType
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PositionDto (
	val itemId: String,
	val favouriteType: TransitType
)