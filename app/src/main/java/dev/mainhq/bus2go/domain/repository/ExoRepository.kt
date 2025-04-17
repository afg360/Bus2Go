package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.core.Result
import java.time.LocalDate

interface ExoRepository {

	suspend fun getMaxEndDate() : Result<LocalDate?>

	//suspend fun getBusDir(routeId: String) : String
	/** Queries for buses and trains with a name matching with the query. */
	suspend fun getRouteInfo(routeId : FuzzyQuery) : Result<List<RouteInfo>>

	/**
	 * @param direction1 AKA headsign
	 * @param direction2 May be null in the case where we only have one direction available.
	 * @return A pair of a list of stopNames in both directions. If only one direction is available,
	 * the second list is empty.
	 * */
	suspend fun getBusStopNames(direction1 : String, direction2: String?) : Result<Pair<List<String>, List<String>>>
	suspend fun getTrainStopNames(routeId : String) : Result<Pair<List<String>, List<String>>>
	suspend fun getBusStopTimes(exoBusItem: ExoBusItem, curTime: Time) : Result<List<Time>>
	//TODO? renaming of the method to getBusOldStopTimes and for trains...?
	suspend fun getOldStopTimes(exoTransitData: TransitData, curTime: Time) : Result<List<Time>>
	suspend fun getFavouriteBusStopTime(exoFavouriteBusItem: ExoBusItem, curTime: Time) : Result<TransitDataWithTime>
	suspend fun getTrainStopTimes(exoTrainItem: ExoTrainItem, curTime: Time) : Result<List<Time>>
	suspend fun getFavouriteTrainStopTime(exoFavouriteTrainItem: ExoTrainItem, curTime: Time) : Result<TransitDataWithTime>

	suspend fun getBusTripHeadsigns(routeId : String) : Result<List<String>>
	suspend fun getTrainTripHeadsigns(routeId : Int, directionId: Int) : Result<List<String>> //not used by anything right now...
}