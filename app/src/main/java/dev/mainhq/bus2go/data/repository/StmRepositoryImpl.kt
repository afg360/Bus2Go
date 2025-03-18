package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDatesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsInfoDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.TripsDAO
import dev.mainhq.bus2go.domain.entity.FavouriteTransitInfo
import dev.mainhq.bus2go.domain.entity.stm.CalendarDates
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.StmBusData
import dev.mainhq.bus2go.utils.FuzzyQuery

class StmRepositoryImpl(
	private val calendarDAO: CalendarDAO,
	private val calendarDatesDAO: CalendarDatesDAO,
	private val routesDAO: RoutesDAO,
	private val stopsDAO: StopsDAO,
	private val stopsInfoDAO: StopsInfoDAO,
	private val tripsDAO: TripsDAO
): StmRepository {
	override suspend fun getMaxEndDate() = calendarDAO.getMaxEndDate()

	override suspend fun getAllCalendarDates(): List<CalendarDates> {
		return calendarDatesDAO.getAllCalendarDates()
			.map { CalendarDates(it.serviceId, it.date, it.exceptionType) }
	}

	override suspend fun getBusDir() = routesDAO.getBusDir()
	override suspend fun getBusRouteInfo(routeId: FuzzyQuery) = routesDAO.getBusRouteInfo(routeId)

	override suspend fun getStopName(stopId: Int) = stopsDAO.getStopName(stopId)

	override suspend fun getStopNames(headsign: String, routeId: String) = stopsInfoDAO.getStopNames(headsign, routeId)

	override suspend fun getStopTimes(stopName: String, day: String, curTime: String,
		headsign: String, routeId: Int, curDate: String) = stopsInfoDAO.getStopTimes(stopName, day,
			curTime, headsign, routeId, curDate
		)

	/** Used for alarms... */
	override suspend fun getStopTimes(stopName: String, day: String, headsign: String, routeId: Int,
		curDate: String ) = stopsInfoDAO.getStopTimes(stopName, day, headsign, routeId, curDate)

	override suspend fun getOldTimes(stopName: String, day: String, curTime: String, headsign: String,
		routeId: String) = stopsInfoDAO.getOldTimes(stopName, day, curTime, headsign, routeId)

	override suspend fun getFavouriteStopTime(list: List<StmBusData>,
											  stopName: String, day: String, time: String,
											  headsign: String, routeId: Int, curDate: String) {
		val times = mutableListOf<FavouriteTransitInfo>()
		list.forEach { transitInfo ->

		}
		stopsInfoDAO.getFavouriteStopTime(stopName,
			day, time, headsign, routeId, curDate)

	}
	override suspend fun getDirectionInfo(routeId: Int) = tripsDAO.getDirectionInfo(routeId)
}