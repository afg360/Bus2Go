package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.stm.CalendarDates
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.stm.StmFavouriteBusItem
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import java.time.LocalDate

/* Create an interface making testing much easier, since we can mock data */
interface StmRepository {

	//FIXME use Time objects instead of multiple strings

	/** @return Latest calendar date before data not being up to date. **/
	suspend fun getMaxEndDate() : LocalDate?

	suspend fun getAllCalendarDates(): List<CalendarDates>

	suspend fun getBusDir() : List<String>;
	suspend fun getBusRouteInfo(routeId : FuzzyQuery) : List<RouteInfo>

	suspend fun getStopName(stopId : Int) : String

	suspend fun getStopNames(headsigns: Pair<String, String>, routeId : String) : Pair<List<String>, List<String>>

	suspend fun getOldTimes(stmTransitData: FavouriteTransitData, curTime: Time): List<Time>
	suspend fun getStopTimes(stmTransitData: FavouriteTransitData, curTime: Time) : List<Time>
	/**
	 * Used for creating new alarm
	 **/
	suspend fun getStopTimes(stopName : String, headsign: String, routeId: Int, curTime: Time) : List<Time>
	/** @param curTime The current time. The stop time retrieved will correspond to the one nearest to now */
	//FIXME instead of querying once the favourite thing, query all the stop times and cache them somewhere
	suspend fun getFavouriteStopTime(stmFavouriteBusItem: StmFavouriteBusItem, curTime: Time) : FavouriteTransitDataWithTime

	suspend fun getDirectionInfo(routeId : Int) : List<DirectionInfo>
}