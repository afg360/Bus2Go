package app.bus2go.android.domain.use_case.transit

import app.bus2go.android.domain.core.Result
import app.bus2go.android.domain.entity.ExoBusRouteInfo
import app.bus2go.android.domain.entity.ExoTrainRouteInfo
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.domain.entity.StmBusRouteInfo
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.entity.stm.DirectionInfo
import app.bus2go.android.domain.repository.TransitRepository
import app.bus2go.android.utils.queryRepos

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