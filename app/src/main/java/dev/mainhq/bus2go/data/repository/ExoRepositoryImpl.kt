package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.StopTimesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.TripsDAO
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteTrainItem
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time

class ExoRepositoryImpl(
	private val calendarDAO: CalendarDAO,
	private val routesDAO: RoutesDAO,
	private val stopTimesDAO: StopTimesDAO,
	private val tripsDAO: TripsDAO
): ExoRepository {
	override suspend fun getMaxEndDate() = calendarDAO.getMaxEndDate()

	override suspend fun getBusDir(routeId: String) = routesDAO.getBusDir(routeId)
	override suspend fun getBusRouteInfo(routeId: FuzzyQuery) = routesDAO.getBusRouteInfo(routeId)

	override suspend fun getStopNames(direction: String) = stopTimesDAO.getStopNames(direction)
	override suspend fun getTrainStopNames(routeId: String, directionId: Int) = stopTimesDAO.getTrainStopNames(routeId, directionId)
	override suspend fun getStopTimes(exoTransitData: FavouriteTransitData, curTime: Time) =
		stopTimesDAO.getStopTimes(
			exoTransitData.stopName,
			curTime.getDayString(),
			curTime.getTimeString(),
			exoTransitData.direction,
			curTime.getTodayString()
		)

	override suspend fun getOldStopTimes(exoTransitData: FavouriteTransitData, curTime: Time) =
		stopTimesDAO.getOldStopTimes(
			exoTransitData.stopName,
			curTime.getDayString(),
			curTime.getTimeString(),
			exoTransitData.direction
		)

	override suspend fun getFavouriteBusStopTime(exoFavouriteBusItem: ExoFavouriteBusItem, curTime: Time) =
		FavouriteTransitDataWithTime(
			exoFavouriteBusItem,
			stopTimesDAO.getFavouriteBusStopTime(
				exoFavouriteBusItem.stopName,
				curTime.getDayString(),
				curTime.getTimeString(),
				exoFavouriteBusItem.headsign,
				curTime.getDayString()
			)
		)

	override suspend fun getTrainStopTimes(exoTrainItem: ExoFavouriteTrainItem, curTime: Time) =
		stopTimesDAO.getTrainStopTimes(
			exoTrainItem.routeId,
			exoTrainItem.stopName,
			exoTrainItem.directionId,
			curTime.getTimeString(),
			curTime.getDayString(),
			curTime.getTodayString()
		)

	override suspend fun getFavouriteTrainStopTime(exoFavouriteTrainItem: ExoFavouriteTrainItem, curTime: Time) =
		FavouriteTransitDataWithTime(
			exoFavouriteTrainItem,
			stopTimesDAO.getFavouriteTrainStopTime(exoFavouriteTrainItem.routeId,
				exoFavouriteTrainItem.stopName,
				exoFavouriteTrainItem.directionId,
				curTime.getTimeString(),
				curTime.getDayString(),
				curTime.getTodayString()
			)
		)

	override suspend fun getTripHeadsigns(routeId: String) = tripsDAO.getTripHeadsigns(routeId)

	override suspend fun getRouteId() = tripsDAO.getRouteId()

	override suspend fun getRouteId(headsign: String) = tripsDAO.getRouteId(headsign)
}