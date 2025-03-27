package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.DbMapper
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.StopTimesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.TripsDAO
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class ExoRepositoryImpl(
	private val calendarDAO: CalendarDAO,
	private val routesDAO: RoutesDAO,
	private val stopTimesDAO: StopTimesDAO,
	private val tripsDAO: TripsDAO
): ExoRepository {
	override suspend fun getMaxEndDate() = calendarDAO.getMaxEndDate()

	override suspend fun getBusDir(routeId: String) = routesDAO.getBusDir(routeId)

	override suspend fun getRouteInfo(routeId: FuzzyQuery): List<RouteInfo> {
		return routesDAO.getRouteInfo(routeId).toMutableList().map {
			DbMapper.mapFromExoDbRouteInfoDtoToRouteInfo(it)
		}.toList()
	}

	override suspend fun getStopNames(directions: Pair<String, String>): Pair<List<String>, List<String>> {
		return withContext(Dispatchers.IO) {
			val job1 = async { stopTimesDAO.getStopNames(directions.first) }
			val job2 = async { stopTimesDAO.getStopNames(directions.second) }
			Pair(job1.await(), job2.await())
		}
	}

	override suspend fun getTrainStopNames(routeId: String): Pair<List<String>, List<String>>{
		return withContext(Dispatchers.IO) {
			val job1 = async { stopTimesDAO.getTrainStopNames("trains-$routeId", 0) }
			val job2 = async { stopTimesDAO.getTrainStopNames("trains-$routeId", 1) }
			Pair(job1.await(), job2.await())
		}
	}

	override suspend fun getStopTimes(exoTransitData: TransitData, curTime: Time) =
		withContext(Dispatchers.IO){
			stopTimesDAO.getStopTimes(
				exoTransitData.stopName,
				curTime.getDayString(),
				curTime.getTimeString(),
				exoTransitData.direction,
				curTime.getTodayString()
			)
		}

	override suspend fun getOldStopTimes(exoTransitData: TransitData, curTime: Time) =
		withContext(Dispatchers.IO){
			stopTimesDAO.getOldStopTimes(
				exoTransitData.stopName,
				curTime.getDayString(),
				curTime.getTimeString(),
				exoTransitData.direction
			)
		}

	override suspend fun getFavouriteBusStopTime(exoFavouriteBusItem: ExoBusItem, curTime: Time) =
		withContext(Dispatchers.IO){
			TransitDataWithTime(
				exoFavouriteBusItem,
				stopTimesDAO.getFavouriteBusStopTime(
					exoFavouriteBusItem.stopName,
					curTime.getDayString(),
					curTime.getTimeString(),
					exoFavouriteBusItem.headsign,
					curTime.getDayString()
				)
			)
		}


	override suspend fun getTrainStopTimes(exoTrainItem: ExoTrainItem, curTime: Time) =
		withContext(Dispatchers.IO){
			stopTimesDAO.getTrainStopTimes(
				exoTrainItem.routeId,
				exoTrainItem.stopName,
				exoTrainItem.directionId,
				curTime.getTimeString(),
				curTime.getDayString(),
				curTime.getTodayString()
			)
		}


	override suspend fun getFavouriteTrainStopTime(exoFavouriteTrainItem: ExoTrainItem, curTime: Time) =
		withContext(Dispatchers.IO){
			TransitDataWithTime(
				exoFavouriteTrainItem,
				stopTimesDAO.getFavouriteTrainStopTime(exoFavouriteTrainItem.routeId,
					exoFavouriteTrainItem.stopName,
					exoFavouriteTrainItem.directionId,
					curTime.getTimeString(),
					curTime.getDayString(),
					curTime.getTodayString()
				)
			)
		}


	override suspend fun getTripHeadsigns(routeId: String) = withContext(Dispatchers.IO){
		tripsDAO.getTripHeadsigns(routeId)
	}

	override suspend fun getRouteId() = withContext(Dispatchers.IO) {
		tripsDAO.getRouteId()
	}

	override suspend fun getRouteId(headsign: String) = withContext(Dispatchers.IO) {
		tripsDAO.getRouteId(headsign)
	}
}