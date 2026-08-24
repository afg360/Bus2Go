package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.DbMapper
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.StopTimesDAO
import dev.mainhq.bus2go.data.data_source.local.database.exo.dao.TripsDAO
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.repository.TransitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.LocalDate

//TODO handle if the result is null for when user closed and deleted the database
class ExoRepositoryImpl(
	private val calendarDAO: CalendarDAO?,
	private val routesDAO: RoutesDAO?,
	private val stopTimesDAO: StopTimesDAO?,
	private val tripsDAO: TripsDAO?,
): TransitRepository {

	override val transitType = TransitType.EXO_BUS
	override val dbName = DatabaseAgency.EXO.toString()

	override suspend fun getDatabaseExpirationDate(): Result<LocalDate> {
		return calendarDAO?.let{ Result.Success(it.getExpirationDate()) } ?: Result.Error(null)
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

	override suspend fun getStopNames(direction1: String, direction2: String?, routeId: String?)
	: Result<Pair<List<String>, List<String>>> {
		return stopTimesDAO?.let { dao ->
			withContext(Dispatchers.IO) {
				val job1 = async { dao.getStopNames(direction1) }
				direction2?.let {
					Result.Success( Pair(job1.await(), async { dao.getStopNames(it) }.await()) )
				} ?: Result.Success(Pair(job1.await(), emptyList()))
			}
		} ?: Result.Error(null)
	}

	override suspend fun getStopTimes(transitData: TransitData, curTime: Time): Result<List<Time>> {
		transitData as ExoBusItem
		return stopTimesDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(
					it.getStopTimes(
						transitData.stopName,
						curTime.getDayString(),
						curTime.getTimeString(),
						transitData.direction,
						curTime.getTodayString()
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getOldStopTimes(transitData: TransitData, curTime: Time): Result<List<Time>> {
		transitData as ExoBusItem
		return stopTimesDAO?.let{
			withContext(Dispatchers.IO){
				Result.Success(
					it.getOldStopTimes(
						transitData.stopName,
						curTime.getDayString(),
						curTime.getTimeString(),
						transitData.direction
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getFavouriteStopTime( favouriteTransitData: FavouriteTransitData, curTime: Time)
	: Result<FavouriteTransitDataWithTime> {
		favouriteTransitData as ExoBusFavouriteItem
		return stopTimesDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(
					FavouriteTransitDataWithTime(
						favouriteTransitData,
						it.getFavouriteBusStopTime(
							favouriteTransitData.stopName,
							curTime.getDayString(),
							curTime.getTimeString(),
							favouriteTransitData.direction,
							curTime.getTodayString()
						)
					)
				)
			}
		} ?: Result.Error(null)
	}

	override suspend fun getTripHeadsigns(routeId: String): Result<List<DirectionInfo>> {
		return tripsDAO?.let {
			withContext(Dispatchers.IO){
				Result.Success(it.getBusTripHeadsigns(routeId).map { DirectionInfo.ExoBusDirectionInfo(it) })
			}
		} ?: Result.Error(null)
	}

}