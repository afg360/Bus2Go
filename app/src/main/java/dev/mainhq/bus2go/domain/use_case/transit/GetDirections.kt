package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.repository.TransitRepository
import dev.mainhq.bus2go.utils.queryRepos

class GetDirections(
	private val transitRepos: List<TransitRepository>
) {

	/**
	 * @return If an ExoBus, returns a list of Strings. If an StmBus returns a list of DirectionInfo.
	 * @throws IllegalArgumentException When an ExoTrainRouteInfo is passed as an argument for routeInfo.
	 **/
	suspend operator fun invoke(routeInfo: RouteInfo): Result<List<DirectionInfo>> {
		return when(routeInfo){
			is StmBusRouteInfo -> {
				transitRepos.queryRepos(TransitType.STM).getTripHeadsigns(routeInfo.routeId)
			}
			is ExoBusRouteInfo -> {
				transitRepos.queryRepos(TransitType.EXO_BUS).getTripHeadsigns(routeInfo.routeId)
			}
			is ExoTrainRouteInfo -> Result.Error(IllegalArgumentException("You're not supposed to use this class for trains."))
		}

	}
}