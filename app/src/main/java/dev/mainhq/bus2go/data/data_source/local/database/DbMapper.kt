package dev.mainhq.bus2go.data.data_source.local.database

import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo

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
				/** Train number (WHICH IS != TO THE ROUTE_ID */
				values[0].toInt()
			)
		}
		else return ExoBusRouteInfo(tmp[1], routeInfoDto.routeName)
	}

}