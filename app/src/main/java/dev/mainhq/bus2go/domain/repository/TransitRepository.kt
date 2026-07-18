package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import java.time.LocalDate

/**
 * Must be implemented for all transit agencies used in the app
 * */
interface TransitRepository {
	val transitType: TransitType
	val dbName: String

	/** @return Latest calendar date before data not being up to date. **/
	suspend fun getDatabaseExpirationDate(): Result<LocalDate>

	/** Queries for buses and trains with a name matching with the query. */
	suspend fun getRouteInfo(routeId: FuzzyQuery): Result<List<RouteInfo>>

	//FIXME do not output a pair, do that at the use case level
	//FIXME naming of parameters
	/**
	 * @param direction1 May be a direction, a headsign depending on the agency
	 **/
	suspend fun getStopNames(direction1: String, direction2: String?, routeId: String?) : Result<Pair<List<String>, List<String>>>

	suspend fun getStopTimes(transitData: TransitData, curTime: Time) : Result<List<Time>>

	suspend fun getOldStopTimes(transitData: TransitData, curTime: Time) : Result<List<Time>>

	suspend fun getFavouriteStopTime(favouriteTransitData: FavouriteTransitData, curTime: Time) : Result<FavouriteTransitDataWithTime>

	suspend fun getTripHeadsigns(routeId : String) : Result<List<DirectionInfo>>
}