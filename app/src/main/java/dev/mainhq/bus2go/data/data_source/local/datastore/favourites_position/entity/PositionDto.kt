package dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity

import android.annotation.SuppressLint
import dev.mainhq.bus2go.domain.entity.TransitType
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PositionDto (
	val itemId: String,
	val favouriteType: TransitType
)