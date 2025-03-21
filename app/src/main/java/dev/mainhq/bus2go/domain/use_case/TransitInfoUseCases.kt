package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.use_case.transit.GetRouteNames
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime

data class TransitInfoUseCases(
	val getRouteNames: GetRouteNames,
	val getStopNames: GetStopNames,
	val getBusTime: GetTransitTime
)
