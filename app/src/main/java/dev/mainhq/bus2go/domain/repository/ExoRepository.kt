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

	/**
	 * @param direction1 AKA headsign
	 * @param direction2 May be null in the case where we only have one direction available.
	 * @return A pair of a list of stopNames in both directions. If only one direction is available,
	 * the second list is empty.
	 * */
	suspend fun getStopNames(direction1 : String, direction2: String?) : Pair<List<String>, List<String>>
	suspend fun getTrainStopNames(routeId : String) : Pair<List<String>, List<String>>
	suspend fun getStopTimes(exoBusItem: ExoBusItem, curTime: Time) : List<Time>
	suspend fun getOldStopTimes(exoTransitData: TransitData, curTime: Time) : List<Time>
	suspend fun getFavouriteBusStopTime(exoFavouriteBusItem: ExoBusItem, curTime: Time) : TransitDataWithTime
	suspend fun getTrainStopTimes(exoTrainItem: ExoTrainItem, curTime: Time) : List<Time>
	suspend fun getFavouriteTrainStopTime(exoFavouriteTrainItem: ExoTrainItem, curTime: Time) : TransitDataWithTime

	suspend fun getBusTripHeadsigns(routeId : String) : List<String>
	suspend fun getTrainTripHeadsigns(routeId : Int, directionId: Int) : List<String> //not used by anything right now...
}