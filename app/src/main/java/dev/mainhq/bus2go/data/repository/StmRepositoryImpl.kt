package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.DbMapper
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDatesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsInfoDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.TripsDAO
import dev.mainhq.bus2go.data.data_source.local.preference.PreferenceMapper
import dev.mainhq.bus2go.domain.entity.stm.CalendarDates
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmFavouriteBusItem
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class StmRepositoryImpl(
	private val calendarDAO: CalendarDAO,
	private val calendarDatesDAO: CalendarDatesDAO,
	private val routesDAO: RoutesDAO,
	private val stopsDAO: StopsDAO,
	private val stopsInfoDAO: StopsInfoDAO,
	private val tripsDAO: TripsDAO
): StmRepository {

	override suspend fun getMaxEndDate() = withContext(Dispatchers.IO){
		calendarDAO.getMaxEndDate()
	}

	override suspend fun getAllCalendarDates(): List<CalendarDates> {
		return withContext(Dispatchers.IO) {
			calendarDatesDAO.getAllCalendarDates()
				.map { CalendarDates(it.serviceId, it.date, it.exceptionType) }
		}
	}

	override suspend fun getBusDir() = withContext(Dispatchers.IO) {
		routesDAO.getBusDir()
	}

	override suspend fun getBusRouteInfo(routeId: FuzzyQuery): List<RouteInfo> {
		return withContext(Dispatchers.IO) {
			routesDAO.getBusRouteInfo(routeId).toMutableList().map {
				DbMapper.mapFromStmDbRouteInfoDtoToRouteInfo(it)
			}.toList()
		}
	}

	override suspend fun getStopName(stopId: Int) = withContext(Dispatchers.IO) {
		stopsDAO.getStopName(stopId)
	}

	override suspend fun getStopNames(headsigns: Pair<String, String>, routeId: String): Pair<List<String>, List<String>> {
		return withContext(Dispatchers.IO) {
			val job1 = async{ stopsInfoDAO.getStopNames(headsigns.first, routeId) }
			val job2 = async{ stopsInfoDAO.getStopNames(headsigns.second, routeId) }
			Pair(job1.await(), job2.await())
		}
	}

	override suspend fun getStopTimes(stmTransitData: FavouriteTransitData, curTime: Time) =
		withContext(Dispatchers.IO) {
			stopsInfoDAO.getStopTimes(
				stmTransitData.stopName,
				curTime.getDayString(),
				curTime.getTimeString(),
				stmTransitData.direction,
				stmTransitData.routeId.toInt(),
				curTime.getTodayString()
			)
		}

	/** Used for alarms... */
	override suspend fun getStopTimes(stopName: String, headsign: String, routeId: Int, curTime: Time) =
		withContext(Dispatchers.IO) {
			stopsInfoDAO.getStopTimes(stopName, curTime.getDayString(), headsign, routeId, curTime.getDayString())
		}

	override suspend fun getOldTimes(stmTransitData: FavouriteTransitData, curTime: Time) =
		withContext(Dispatchers.IO) {
			stopsInfoDAO.getOldTimes(
				stmTransitData.stopName,
				curTime.getDayString(),
				curTime.getTimeString(),
				stmTransitData.direction,
				stmTransitData.routeId
			)
		}

	override suspend fun getFavouriteStopTime(
		stmFavouriteBusItem: StmFavouriteBusItem,
		curTime: Time
	): FavouriteTransitDataWithTime {
		return withContext(Dispatchers.IO){
			val stmFavouriteBusItemDto = PreferenceMapper.mapStmBusToDto(stmFavouriteBusItem)
			stopsInfoDAO.getFavouriteStopTime(
				stmFavouriteBusItemDto.stopName,
				curTime.getDayString(),
				curTime.getTimeString(),
				stmFavouriteBusItemDto.direction,
				stmFavouriteBusItemDto.routeId.toInt(),
				curTime.getTodayString()
			).let { FavouriteTransitDataWithTime(stmFavouriteBusItem, it) }

		}
	}

	override suspend fun getDirectionInfo(routeId: Int) = withContext(Dispatchers.IO) {
		tripsDAO.getDirectionInfo(routeId)
	}
}