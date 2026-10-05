package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.local.database.DbMapper
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
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
import dev.mainhq.bus2go.domain.repository.DatabaseControllerRepository
import dev.mainhq.bus2go.domain.repository.TransitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

//TODO handle if the result is null for when user closed and deleted the database
class ExoRepositoryImpl(
	private val databaseControllerRepository: DatabaseControllerRepository
): TransitRepository {

	override val transitType = TransitType.EXO_BUS
	override val dbName = DatabaseAgency.EXO.toString()

	@OptIn(ExperimentalCoroutinesApi::class)
	override val databaseExpirationDate: Flow<Result<LocalDate>> =
		(databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO))
			.flatMapLatest {
				(it as AppDatabaseExo?)?.calendarDao()
					?.getExpirationDate()?.map {
						Result.Success(it)
					} ?: flowOf(Result.Error(null))
			}


	override suspend fun getRouteInfo(routeId: FuzzyQuery): Result<List<RouteInfo>> {
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.routesDao()?.let{
				Result.Success(
					it.getRouteInfo(routeId).toMutableList().map {
						DbMapper.mapFromExoDbRouteInfoDtoToRouteInfo(it)
					}.toList()
				)
			} ?: Result.Error(null)
	}

	override suspend fun getStopNames(direction1: String, direction2: String?, routeId: String?)
			: Result<Pair<List<String>, List<String>>> {
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.stopTimesDao()?.let { dao ->
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
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.stopTimesDao()?.let { dao ->
				withContext(Dispatchers.IO){
					Result.Success(
						dao.getStopTimes(
							transitData.stopName,
							curTime.getQueryDayString(),
							curTime.getQueryTimeString(),
							transitData.direction,
							curTime.getQueryTodayString()
						)
					)
				}
			} ?: Result.Error(null)
	}

	override suspend fun getOldStopTimes(transitData: TransitData, curTime: Time): Result<List<Time>> {
		transitData as ExoBusItem
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.stopTimesDao()?.let { dao ->
				withContext(Dispatchers.IO){
					Result.Success(
						dao.getOldStopTimes(
							transitData.stopName,
							curTime.getQueryDayString(),
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
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.stopTimesDao()?.let { dao ->
				withContext(Dispatchers.IO){
					Result.Success(
						FavouriteTransitDataWithTime(
							favouriteTransitData,
							dao.getFavouriteBusStopTime(
								favouriteTransitData.stopName,
								curTime.getQueryDayString(),
								curTime.getTimeString(),
								favouriteTransitData.direction,
								curTime.getQueryTodayString()
							)
						)
					)
				}
			} ?: Result.Error(null)
	}

	override suspend fun getTripHeadsigns(routeId: String): Result<List<DirectionInfo>> {
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.tripsDao()?.let {
				withContext(Dispatchers.IO){
					Result.Success(it.getBusTripHeadsigns(routeId).map { DirectionInfo.ExoBusDirectionInfo(it) })
				}
			} ?: Result.Error(null)
	}

}