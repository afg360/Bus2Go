package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.Time
import java.time.LocalDate

interface ExoRepository {

	suspend fun getMaxEndDate() : LocalDate?

	//suspend fun getBusDir(routeId: String) : String
	/** Queries for buses and trains with a name matching with the query. */
	suspend fun getRouteInfo(routeId : FuzzyQuery) : List<RouteInfo>

	/** @param direction AKA headsign */
	suspend fun getStopNames(directions : Pair<String, String>) : Pair<List<String>, List<String>>
	suspend fun getTrainStopNames(routeId : String) : Pair<List<String>, List<String>>
	suspend fun getStopTimes(exoBusItem: ExoBusItem, curTime: Time) : List<Time>
	suspend fun getOldStopTimes(exoTransitData: TransitData, curTime: Time) : List<Time>
	suspend fun getFavouriteBusStopTime(exoFavouriteBusItem: ExoBusItem, curTime: Time) : TransitDataWithTime
	suspend fun getTrainStopTimes(exoTrainItem: ExoTrainItem, curTime: Time) : List<Time>
	suspend fun getFavouriteTrainStopTime(exoFavouriteTrainItem: ExoTrainItem, curTime: Time) : TransitDataWithTime

	suspend fun getTripHeadsigns(routeId : String) : List<String>
}