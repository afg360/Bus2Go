package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.DbMapper
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.StopTimesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.TripsDAO
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ExoRepositoryImpl(
	private val calendarDAO: CalendarDAO?,
	private val routesDAO: RoutesDAO?,
	private val stopTimesDAO: StopTimesDAO?,
	private val tripsDAO: TripsDAO?
): ExoRepository {

	override suspend fun getMaxEndDate(): Result<LocalDate> {
		return calendarDAO?.let{ Result.Success(it.getMaxEndDate()) } ?: Result.Error(null)
	}

	override suspend fun getRouteInfo(routeId: FuzzyQuery): Result<List<RouteInfo>> {
		return routesDAO?.let{
			Result.Success(
				it.getRouteInfo(routeId).toMutableList().map {
					DbMapper.mapFromExoDbRouteInfoDtoToRouteInfo(it)
				}.toList()
			)
		} ?: Result.Error(null)
	}

	override suspend fun getBusStopNames(direction1: String, direction2: String?): Result<Pair<List<String>, List<String>>> {
		return stopTimesDAO?.let { dao ->
			withContext(Dispatchers.IO) {
					val job1 = async { dao.getStopNames(direction1) }
					direction2?.let {
						Result.Success( Pair(job1.await(), async { dao.getStopNames(it) }.await()) )
					} ?: Result.Success(Pair(job1.await(), emptyList()))
			}
		} ?: Result.Error(null)
	}

	override suspend fun getTrainStopNames(routeId: String): Result<Pair<List<String>, List<String>>>{
		return stopTimesDAO?.let {
			withContext(Dispatchers.IO) {
				val job1 = async { it.getTrainStopNames("trains-$routeId", 0) }
				val job2 = async { it.getTrainStopNames("trains-$routeId", 1) }
				Result.Success(Pair(job1.await(), job2.await()))
			}
		} ?: Result.Error(null)
	}

	override suspend fun getBusStopTimes(exoBusItem: ExoBusItem, curTime: Time): Result<List<Time>> {
		return stopTimesDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(
					it.getStopTimes(
						exoBusItem.stopName,
						curTime.getDayString(),
						curTime.getTimeString(),
						exoBusItem.direction,
						curTime.getTodayString()
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getOldStopTimes(exoTransitData: TransitData, curTime: Time): Result<List<Time>> {
		return stopTimesDAO?.let{
			withContext(Dispatchers.IO){
				Result.Success(
					it.getOldStopTimes(
						exoTransitData.stopName,
						curTime.getDayString(),
						curTime.getTimeString(),
						exoTransitData.direction
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getFavouriteBusStopTime(
		exoFavouriteBusItem: ExoBusItem,
		curTime: Time,
	): Result<TransitDataWithTime> {
		return stopTimesDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(
					TransitDataWithTime(
						exoFavouriteBusItem,
						it.getFavouriteBusStopTime(
							exoFavouriteBusItem.stopName,
							curTime.getDayString(),
							curTime.getTimeString(),
							exoFavouriteBusItem.headsign,
							curTime.getDayString()
						)
					)
				)
			}
		} ?: Result.Error(null)
	}


	override suspend fun getTrainStopTimes(exoTrainItem: ExoTrainItem, curTime: Time): Result<List<Time>> {
		return stopTimesDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(
					it.getTrainStopTimes(
						exoTrainItem.routeId,
						exoTrainItem.stopName,
						exoTrainItem.directionId,
						curTime.getTimeString(),
						curTime.getDayString(),
						curTime.getTodayString()
					)
				)
			}
		} ?: Result.Error(null)
	}


	override suspend fun getFavouriteTrainStopTime(
		exoFavouriteTrainItem: ExoTrainItem,
		curTime: Time,
	) : Result<TransitDataWithTime> {
		return stopTimesDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(
					TransitDataWithTime(
						exoFavouriteTrainItem,
						it.getFavouriteTrainStopTime(exoFavouriteTrainItem.routeId,
							exoFavouriteTrainItem.stopName,
							exoFavouriteTrainItem.directionId,
							curTime.getTimeString(),
							curTime.getDayString(),
							curTime.getTodayString()
						)
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getBusTripHeadsigns(routeId: String): Result<List<DirectionInfo>> {
		return tripsDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(it.getBusTripHeadsigns(routeId).map { DirectionInfo.ExoBusDirectionInfo(it) })
			}
		} ?: Result.Error(null)
	}

	override suspend fun getTrainTripHeadsigns(routeId: Int, directionId: Int): Result<List<String>> {
		return tripsDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(it.getTrainTripHeadsigns(routeId, directionId))
			}
		} ?: Result.Error(null)
	}

}