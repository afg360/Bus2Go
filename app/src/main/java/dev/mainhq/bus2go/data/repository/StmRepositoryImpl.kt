package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.data.data_source.local.database.DbMapper
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDatesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.FeedInfoDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsInfoDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.TripsDAO
import dev.mainhq.bus2go.data.data_source.local.datastore.PreferenceMapper
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.repository.TransitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StmRepositoryImpl(
	private val feedInfoDAO: FeedInfoDAO?,
	private val calendarDatesDAO: CalendarDatesDAO?,
	private val routesDAO: RoutesDAO?,
	private val stopsDAO: StopsDAO?,
	private val stopsInfoDAO: StopsInfoDAO?,
	private val tripsDAO: TripsDAO?,
): TransitRepository {

	override val transitType = TransitType.STM
	override val dbName = DatabaseAgency.STM.toString()

	override suspend fun getDatabaseExpirationDate(): Result<LocalDate> {
		return feedInfoDAO?.let{
			withContext(Dispatchers.IO){ Result.Success(it.getExpirationDate()) }
		} ?: Result.Error(null)
	}

	override suspend fun getRouteInfo(routeId: FuzzyQuery): Result<List<RouteInfo>> {
		return routesDAO?.let{
			withContext(Dispatchers.IO) {
				Result.Success(it.getBusRouteInfo(routeId).toMutableList()
					.map { item -> DbMapper.mapFromStmDbRouteInfoDtoToRouteInfo(item) }
					.toList()
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getStopNames(direction1: String, direction2: String?, routeId: String?)
	: Result<Pair<List<String>, List<String>>> {
		return stopsInfoDAO?.let {
			withContext(Dispatchers.IO) {
				val job1 = async{ it.getStopNames(direction1, routeId!!) }
				val job2 = async{ it.getStopNames(direction2!!, routeId!!) }
				Result.Success(Pair(job1.await(), job2.await()))
			}
		} ?: Result.Error(null)
	}

	override suspend fun getStopTimes(transitData: TransitData, curTime: Time): Result<List<Time>> {
		return stopsInfoDAO?.let{
			withContext(Dispatchers.IO) {
				Result.Success(
					it.getStopTimes(
						transitData.stopName,
						curTime.getDayString(),
						curTime.getTimeString(),
						transitData.direction,
						transitData.routeId.toInt(),
						curTime.getTodayString()
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getOldStopTimes(transitData: TransitData, curTime: Time): Result<List<Time>> {
		return stopsInfoDAO?.let {
			withContext(Dispatchers.IO) {
				Result.Success(
					it.getOldTimes(
						transitData.stopName,
						curTime.getDayString(),
						curTime.getTimeString(),
						transitData.direction,
						transitData.routeId
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getFavouriteStopTime(favouriteTransitData: FavouriteTransitData, curTime: Time)
	: Result<FavouriteTransitDataWithTime> {
		favouriteTransitData as StmBusFavouriteItem
		return stopsInfoDAO?.let {
			withContext(Dispatchers.IO){
				val stmFavouriteBusItemDto = PreferenceMapper.mapStmBusToDto(favouriteTransitData)
				Result.Success(
					FavouriteTransitDataWithTime(
						favouriteTransitData,
						it.getFavouriteStopTime(
							stmFavouriteBusItemDto.stopName,
							curTime.getDayString(),
							curTime.getTimeString(),
							stmFavouriteBusItemDto.direction,
							stmFavouriteBusItemDto.routeId.toInt(),
							curTime.getTodayString()
						)
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getTripHeadsigns(routeId: String): Result<List<DirectionInfo>> {
		return tripsDAO?.let { tripsDAO ->
			withContext(Dispatchers.IO) {
				Result.Success(tripsDAO.getDirectionInfo(routeId.toInt())
					.map { DirectionInfo.StmDirectionInfo(it.tripHeadSign, it.directionId) }
				)
			}
		} ?: Result.Error(null)
	}
}