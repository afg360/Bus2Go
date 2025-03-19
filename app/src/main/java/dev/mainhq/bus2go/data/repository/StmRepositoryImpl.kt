package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDatesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsInfoDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.TripsDAO
import dev.mainhq.bus2go.data.data_source.local.preference.Mapper
import dev.mainhq.bus2go.domain.entity.stm.CalendarDates
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.data.data_source.local.preference.stm.entity.StmFavouriteBusItemDto
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.stm.StmFavouriteBusItem
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time

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

	override suspend fun getStopTimes(stmTransitData: FavouriteTransitData, curTime: Time) =
		stopsInfoDAO.getStopTimes(
			stmTransitData.stopName,
			curTime.getDayString(),
			curTime.getTimeString(),
			stmTransitData.direction,
			stmTransitData.routeId.toInt(),
			curTime.getTodayString()
		)

	/** Used for alarms... */
	override suspend fun getStopTimes(stopName: String, headsign: String, routeId: Int, curTime: Time) =
		stopsInfoDAO.getStopTimes(stopName, curTime.getDayString(), headsign, routeId, curTime.getDayString())

	override suspend fun getOldTimes(stmTransitData: FavouriteTransitData, curTime: Time) =
		stopsInfoDAO.getOldTimes(
			stmTransitData.stopName,
			curTime.getDayString(),
			curTime.getTimeString(),
			stmTransitData.direction,
			stmTransitData.routeId
		)

	override suspend fun getFavouriteStopTime(stmFavouriteBusItem: StmFavouriteBusItem,
											  curTime: Time): FavouriteTransitDataWithTime {
		val stmFavouriteBusItemDto = Mapper.mapStmBusToDto(stmFavouriteBusItem)
		stopsInfoDAO.getFavouriteStopTime(
			stmFavouriteBusItemDto.stopName,
			curTime.getDayString(),
			curTime.getTimeString(),
			stmFavouriteBusItemDto.direction,
			stmFavouriteBusItemDto.routeId.toInt(),
			curTime.getTodayString()
		).let { return FavouriteTransitDataWithTime(stmFavouriteBusItem, it) }

	}

	override suspend fun getDirectionInfo(routeId: Int) = tripsDAO.getDirectionInfo(routeId)
}