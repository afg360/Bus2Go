package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime

data class TransitTimeInfoUseCases(
	val getRouteInfo: GetRouteInfo,
	val getStopNames: GetStopNames,
	val getBusTime: GetTransitTime
)
