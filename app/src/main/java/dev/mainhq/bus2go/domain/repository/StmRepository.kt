package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.BusRouteInfo
import dev.mainhq.bus2go.domain.entity.stm.CalendarDates
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.StmBusData
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import java.time.LocalDate

/* Create an interface making testing much easier, since we can mock data */
interface StmRepository {

	/** @return Latest calendar date before data not being up to date. **/
	suspend fun getMaxEndDate() : LocalDate?

	suspend fun getAllCalendarDates(): List<CalendarDates>

	suspend fun getBusDir() : List<String>;
	suspend fun getBusRouteInfo(routeId : FuzzyQuery) : List<BusRouteInfo>

	suspend fun getStopName(stopId : Int) : String

	suspend fun getStopNames(headsign : String, routeId : String) : List<String>

	/** @param headsign Direction String. **/
	suspend fun getStopTimes(stopName : String, day : String, curTime : String, headsign: String, routeId: Int, curDate: String) : List<Time>
	suspend fun getOldTimes(stopName: String, day: String, curTime: String, headsign: String, routeId: String): List<Time>
	/** Used for creating new alarm */
	suspend fun getStopTimes(stopName : String, day : String, headsign: String, routeId: Int, curDate: String) : List<Time>
	suspend fun getFavouriteStopTime(list: List<StmBusData>, stopName : String, day : String, time : String, headsign: String, routeId: Int, curDate: String) : Time?

	suspend fun getDirectionInfo(routeId : Int) : List<DirectionInfo>
}