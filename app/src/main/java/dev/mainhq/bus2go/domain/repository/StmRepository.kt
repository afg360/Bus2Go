package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.stm.CalendarDates
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.Time
import java.time.LocalDate

//TODO use the Result pattern to indicate if something worked properly or not...

/* Create an interface making testing much easier, since we can mock data */
interface StmRepository {

	//FIXME use Time objects instead of multiple strings

	/** @return Latest calendar date before data not being up to date. **/
	suspend fun getMaxEndDate() : Result<LocalDate?>

	suspend fun getAllCalendarDates(): Result<List<CalendarDates>>

	suspend fun getBusRouteInfo(routeId : FuzzyQuery) : Result<List<RouteInfo>>

	suspend fun getStopName(stopId : Int) : Result<String>

	suspend fun getStopNames(headsign1: String, headsign2: String, routeId : String)
	: Result<Pair<List<String>, List<String>>>

	suspend fun getOldTimes(stmTransitData: TransitData, curTime: Time): Result<List<Time>>
	suspend fun getStopTimes(stmTransitData: TransitData, curTime: Time) : Result<List<Time>>
	/**
	 * Used for creating new alarm
	 **/
	//suspend fun getStopTimes(stopName : String, headsign: String, routeId: Int, curTime: Time) : List<Time>
	/** @param curTime The current time. The stop time retrieved will correspond to the one nearest to now */
	//FIXME instead of querying once the favourite thing, query all the stop times and cache them somewhere
	suspend fun getFavouriteStopTime(stmFavouriteBusItem: StmBusItem, curTime: Time) : Result<TransitDataWithTime>

	suspend fun getDirectionInfo(routeId : Int) : Result<List<DirectionInfo>>
}