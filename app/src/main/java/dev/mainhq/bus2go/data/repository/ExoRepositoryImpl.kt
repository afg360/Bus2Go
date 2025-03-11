package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.StopTimesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.TripsDAO
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.utils.FuzzyQuery

class ExoRepositoryImpl(
	private val calendarDAO: CalendarDAO,
	private val routesDAO: RoutesDAO,
	private val stopTimesDAO: StopTimesDAO,
	private val tripsDAO: TripsDAO
): ExoRepository {
	override suspend fun getMaxEndDate() = calendarDAO.getMaxEndDate()

	override suspend fun getBusDir(routeId: String) = routesDAO.getBusDir(routeId)
	override suspend fun getBusRouteInfo(routeId: FuzzyQuery) = routesDAO.getBusRouteInfo(routeId)

	override suspend fun getStopNames(headsign: String) = stopTimesDAO.getStopNames(headsign)
	override suspend fun getTrainStopNames(routeId: String, directionId: Int) = stopTimesDAO.getTrainStopNames(routeId, directionId)
	override suspend fun getStopTimes(stopName: String, day: String, curTime: String, headsign: String,
		curDate: String) = stopTimesDAO.getStopTimes(stopName, day, curTime, headsign, curDate)

	override suspend fun getOldStopTimes(stopName: String, day: String, curTime: String,
		 headsign: String) = stopTimesDAO.getOldStopTimes(stopName, day, curTime, headsign)

	override suspend fun getFavouriteBusStopTime(stopName: String, day: String, curTime: String,
		headsign: String, curDate: String) = stopTimesDAO.getFavouriteBusStopTime(
			stopName, day, curTime, headsign, curDate
		)

	override suspend fun getTrainStopTimes(routeId: String, stopName: String, directionId: Int,
		time: String, day: String, curDate: String ) = stopTimesDAO.getTrainStopTimes(
			routeId, stopName, directionId, time, day, curDate
		)

	override suspend fun getFavouriteTrainStopTime(routeId: String, stopName: String,
		directionId: Int, time: String, day: String, curDate: String) =
		stopTimesDAO.getFavouriteTrainStopTime(routeId, stopName, directionId, time, day, curDate)

	override suspend fun getTripHeadsigns(routeId: String) = tripsDAO.getTripHeadsigns(routeId)

	override suspend fun getRouteId() = tripsDAO.getRouteId()

	override suspend fun getRouteId(headsign: String) = tripsDAO.getRouteId(headsign)
}