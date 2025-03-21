package dev.mainhq.bus2go.domain.entity

/**
 * Entities used for getting route information for buses. When passing from search
 * to direction choice activity
 * */
sealed class RouteInfo{
	abstract val routeId : String
	abstract val routeName : String
}

data class StmBusRouteInfo(override val routeId: String, override val routeName: String)
	: RouteInfo()

data class ExoBusRouteInfo(override val routeId: String, override val routeName: String)
	: RouteInfo()

data class ExoTrainRouteInfo(override val routeId: String, override val routeName: String, val trainNum: Int)
	: RouteInfo()
