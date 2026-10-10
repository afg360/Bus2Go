package app.bus2go.android.data.data_source.local.database

import app.bus2go.android.domain.entity.ExoBusRouteInfo
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.domain.entity.ExoTrainRouteInfo
import app.bus2go.android.domain.entity.StmBusRouteInfo

object DbMapper {
	fun mapFromStmDbRouteInfoDtoToRouteInfo(routeInfoDto: RouteInfoDto): RouteInfo{
		return StmBusRouteInfo(routeInfoDto.routeId, routeInfoDto.routeName)
	}

	fun mapFromExoDbRouteInfoDtoToRouteInfo(routeInfoDto: RouteInfoDto): RouteInfo{
		val tmp = routeInfoDto.routeId.split("-", limit = 2)
		if (tmp[0] == "trains") {
			val values = routeInfoDto.routeName.split(" - ", limit = 2)
			return ExoTrainRouteInfo(
				tmp[1],
				/** Parsed train name */
				values[1],
				/** Train number (WHICH IS != TO THE ROUTE_ID) */
				values[0].toInt()
			)
		}
		else return ExoBusRouteInfo(tmp[1], routeInfoDto.routeName)
	}

}