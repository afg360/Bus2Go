package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.BusRouteInfo
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteTrainItem
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import java.time.LocalDate

interface ExoRepository {

	suspend fun getMaxEndDate() : LocalDate?

	suspend fun getBusDir(routeId: String) : String
	suspend fun getBusRouteInfo(routeId : FuzzyQuery) : List<BusRouteInfo>

	/** @param direction AKA headsign */
	suspend fun getStopNames(direction : String) : List<String>
	suspend fun getTrainStopNames(routeId : String, directionId : Int) : List<String>
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