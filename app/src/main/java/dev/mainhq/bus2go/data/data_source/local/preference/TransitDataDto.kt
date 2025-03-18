package dev.mainhq.bus2go.data.data_source.local.preference

import kotlinx.serialization.Serializable

@Serializable
abstract class TransitDataDto {
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
}
