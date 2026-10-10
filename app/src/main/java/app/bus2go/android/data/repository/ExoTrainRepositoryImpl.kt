package app.bus2go.android.data.repository

import app.bus2go.android.domain.core.Result
import app.bus2go.android.data.data_source.local.database.DbMapper
import app.bus2go.android.data.data_source.local.database.exo.AppDatabaseExo
import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.entity.ExoTrainItem
import app.bus2go.android.domain.entity.FavouriteTransitData
import app.bus2go.android.domain.entity.FavouriteTransitDataWithTime
import app.bus2go.android.domain.entity.FuzzyQuery
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.domain.entity.Time
import app.bus2go.android.domain.entity.TransitData
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.entity.stm.DirectionInfo
import app.bus2go.android.domain.exceptions.Bus2GoBaseException
import app.bus2go.android.domain.repository.DatabaseControllerRepository
import app.bus2go.android.domain.repository.TransitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ExoTrainRepositoryImpl(
	private val databaseControllerRepository: DatabaseControllerRepository
): TransitRepository {

	override val transitType = TransitType.EXO_TRAIN
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
			?.stopTimesDao()?.let {
				withContext(Dispatchers.IO) {
					//is actually routeId
					val job1 = async { it.getTrainStopNames("trains-$direction1", 0) }
					val job2 = async { it.getTrainStopNames("trains-$direction1", 1) }
					Result.Success(Pair(job1.await(), job2.await()))
				}
			} ?: Result.Error(null)
	}

	override suspend fun getStopTimes(transitData: TransitData, curTime: Time): Result<List<Time>> {
		transitData as ExoTrainItem
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.stopTimesDao()?.let {
				withContext(Dispatchers.IO){
					Result.Success(
						//TODO replace the curtime calls...
						it.getTrainStopTimes(
							transitData.routeId,
							transitData.stopName,
							transitData.directionId,
							curTime.getQueryTimeString(),
							curTime.getQueryDayString(),
							curTime.getQueryTodayString()
						)
					)
				}
			} ?: Result.Error(null)
	}

	override suspend fun getOldStopTimes(
		transitData: TransitData,
		curTime: Time,
	): Result<List<Time>> {
		transitData as ExoTrainItem
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.stopTimesDao()?.let {
				withContext(Dispatchers.IO){
					Result.Success(
						it.getOldStopTimes(
							transitData.stopName,
							curTime.getQueryDayString(),
							curTime.getQueryTimeString(),
							transitData.direction
						)
					)
				}
			} ?: Result.Error(null)
	}

	override suspend fun getFavouriteStopTime(
		favouriteTransitData: FavouriteTransitData,
		curTime: Time,
	): Result<FavouriteTransitDataWithTime> {
		favouriteTransitData as FavouriteTransitData.ExoTrainFavouriteItem
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.EXO).value as AppDatabaseExo?)
			?.stopTimesDao()?.let {
				withContext(Dispatchers.IO){
					Result.Success(
						FavouriteTransitDataWithTime(
							favouriteTransitData,
							it.getFavouriteTrainStopTime(favouriteTransitData.routeId,
								favouriteTransitData.stopName,
								favouriteTransitData.directionId,
								curTime.getQueryTimeString(),
								curTime.getQueryDayString(),
								curTime.getQueryTodayString()
							)
						)
					)
				}
			} ?: Result.Error(null)
	}

	override suspend fun getTripHeadsigns(routeId: String): Result<List<DirectionInfo>> {
		throw object: Bus2GoBaseException("Not implemented because not used yet") { }
	}

}