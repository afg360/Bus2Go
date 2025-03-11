package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.BusRouteInfo
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import java.time.LocalDate

interface ExoRepository {

	suspend fun getMaxEndDate() : LocalDate?

	suspend fun getBusDir(routeId: String) : String
	suspend fun getBusRouteInfo(routeId : FuzzyQuery) : List<BusRouteInfo>

	suspend fun getStopNames(headsign : String) : List<String>
	suspend fun getTrainStopNames(routeId : String, directionId : Int) : List<String>
	suspend fun getStopTimes(stopName : String, day : String, curTime : String, headsign: String, curDate: String) : List<Time>
	suspend fun getOldStopTimes(stopName : String, day : String, curTime : String, headsign: String) : List<Time>
	suspend fun getFavouriteBusStopTime(stopName : String, day : String, curTime : String, headsign: String, curDate: String) : Time?
	suspend fun getTrainStopTimes(routeId: String, stopName: String, directionId: Int, time: String, day : String, curDate: String) : List<Time>
	suspend fun getFavouriteTrainStopTime(routeId: String, stopName: String, directionId: Int, time: String, day : String, curDate: String) : Time?

	suspend fun getTripHeadsigns(routeId : String) : List<String>
	suspend fun getRouteId() : List<String>
	/** Used to migrate from favourites.json v1 to v2 */
	suspend fun getRouteId(headsign: String): String
}