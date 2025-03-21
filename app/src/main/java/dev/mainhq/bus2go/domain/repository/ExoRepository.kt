package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.ExoFavouriteTrainItem
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import java.time.LocalDate

interface ExoRepository {

	suspend fun getMaxEndDate() : LocalDate?

	suspend fun getBusDir(routeId: String) : String
	/** Queries for buses and trains with a name matching with the query. */
	suspend fun getRouteInfo(routeId : FuzzyQuery) : List<RouteInfo>

	/** @param direction AKA headsign */
	suspend fun getStopNames(directions : Pair<String, String>) : Pair<List<String>, List<String>>
	suspend fun getTrainStopNames(routeId : String) : Pair<List<String>, List<String>>
	suspend fun getStopTimes(exoTransitData: FavouriteTransitData, curTime: Time) : List<Time>
	suspend fun getOldStopTimes(exoTransitData: FavouriteTransitData, curTime: Time) : List<Time>
	suspend fun getFavouriteBusStopTime(exoFavouriteBusItem: ExoFavouriteBusItem, curTime: Time) : FavouriteTransitDataWithTime
	suspend fun getTrainStopTimes(exoTrainItem: ExoFavouriteTrainItem, curTime: Time) : List<Time>
	suspend fun getFavouriteTrainStopTime(exoFavouriteTrainItem: ExoFavouriteTrainItem, curTime: Time) : FavouriteTransitDataWithTime

	suspend fun getTripHeadsigns(routeId : String) : List<String>
	suspend fun getRouteId() : List<String>
	/** Used to migrate from favourites.json v1 to v2 */
	suspend fun getRouteId(headsign: String): String
}