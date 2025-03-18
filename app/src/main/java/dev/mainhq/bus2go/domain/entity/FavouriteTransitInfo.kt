package dev.mainhq.bus2go.domain.entity

import dev.mainhq.bus2go.data.data_source.local.preference.TransitDataDto
import dev.mainhq.bus2go.utils.Time

//FIXME rename
data class FavouriteTransitInfo(
	val transitData: TransitDataDto,
	val arrivalTime : Time?,
	val agency : TransitAgency
)
