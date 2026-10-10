package app.bus2go.android.domain.repository

import app.bus2go.android.domain.core.Result
import app.bus2go.android.domain.entity.FavouriteTransitData
import app.bus2go.android.domain.entity.FavouriteTransitDataWithTime
import app.bus2go.android.domain.entity.FuzzyQuery
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.domain.entity.Time
import app.bus2go.android.domain.entity.TransitData
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.entity.stm.DirectionInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Must be implemented for all transit agencies used in the app
 * */
interface TransitRepository {
	val transitType: TransitType
	val dbName: String

	/** @return Latest calendar date before data not being up to date. **/
	val databaseExpirationDate: Flow<Result<LocalDate>>

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