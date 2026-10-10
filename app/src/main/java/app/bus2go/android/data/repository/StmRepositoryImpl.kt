package app.bus2go.android.data.repository

import app.bus2go.android.domain.core.Result
import app.bus2go.android.data.data_source.local.database.DbMapper
import app.bus2go.android.data.data_source.local.database.stm.AppDatabaseSTM
import app.bus2go.android.data.data_source.local.datastore.PreferenceMapper
import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.entity.FavouriteTransitData
import app.bus2go.android.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import app.bus2go.android.domain.entity.TransitData
import app.bus2go.android.domain.entity.FavouriteTransitDataWithTime
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.domain.entity.FuzzyQuery
import app.bus2go.android.domain.entity.StmBusItem
import app.bus2go.android.domain.entity.Time
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.entity.stm.DirectionInfo
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

class StmRepositoryImpl(
	private val databaseControllerRepository: DatabaseControllerRepository
): TransitRepository {

	override val transitType = TransitType.STM
	override val dbName = DatabaseAgency.STM.toString()

	@OptIn(ExperimentalCoroutinesApi::class)
	override val databaseExpirationDate: Flow<Result<LocalDate>> =
		databaseControllerRepository.getDatabaseFlow(DatabaseAgency.STM)
			.flatMapLatest {
				(it as AppDatabaseSTM?)?.feedInfoDao()?.getExpirationDate()?.map { expDate ->
					expDate?.let {
						Result.Success(it)
					} ?: Result.Error(null)
			} ?: flowOf(Result.Error(null))
	}

	override suspend fun getRouteInfo(routeId: FuzzyQuery): Result<List<RouteInfo>> {
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.STM).value as AppDatabaseSTM?)
			?.routesDao()?.let{
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
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.STM).value as AppDatabaseSTM?)
			?.stopsInfoDao()?.let {
				withContext(Dispatchers.IO) {
					val job1 = async{ it.getStopNames(direction1, routeId!!) }
					val job2 = async{ it.getStopNames(direction2!!, routeId!!) }
					Result.Success(Pair(job1.await(), job2.await()))
				}
			} ?: Result.Error(null)
	}

	override suspend fun getStopTimes(transitData: TransitData, curTime: Time): Result<List<Time>> {
		val headsign = if (transitData.routeId.toInt() <= 5) (transitData as StmBusItem).lastStop
		else transitData.direction

		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.STM).value as AppDatabaseSTM?)
			?.stopsInfoDao()?.let {
				withContext(Dispatchers.IO) {
					Result.Success(
						it.getStopTimes(
							transitData.stopName,
							curTime.getQueryDayString(),
							curTime.getQueryTimeString(),
							headsign,
							transitData.routeId.toInt(),
							curTime.getQueryTodayString()
						)
					)
				}
			} ?: Result.Error(null)
	}

	override suspend fun getOldStopTimes(transitData: TransitData, curTime: Time): Result<List<Time>> {
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.STM).value as AppDatabaseSTM?)
			?.stopsInfoDao()?.let {
				withContext(Dispatchers.IO) {
					Result.Success(
						it.getOldTimes(
							transitData.stopName,
							curTime.getQueryDayString(),
							curTime.getQueryTimeString(),
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
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.STM).value as AppDatabaseSTM?)
			?.stopsInfoDao()?.let {
				withContext(Dispatchers.IO){
					val stmFavouriteBusItemDto = PreferenceMapper.mapStmBusToDto(favouriteTransitData)
					Result.Success(
						FavouriteTransitDataWithTime(
							favouriteTransitData,
							it.getFavouriteStopTime(
								stmFavouriteBusItemDto.stopName,
								curTime.getQueryDayString(),
								curTime.getQueryTimeString(),
								stmFavouriteBusItemDto.direction,
								stmFavouriteBusItemDto.routeId.toInt(),
								curTime.getQueryTodayString()
							)
						)
					)
				}
			} ?: Result.Error(null)
	}

	override suspend fun getTripHeadsigns(routeId: String): Result<List<DirectionInfo>> {
		return (databaseControllerRepository.getDatabaseFlow(DatabaseAgency.STM).value as AppDatabaseSTM?)
			?.tripsDao()?.let { tripsDAO ->
				withContext(Dispatchers.IO) {
					Result.Success(tripsDAO.getDirectionInfo(routeId.toInt())
						.map { DirectionInfo.StmDirectionInfo(it.tripHeadSign, it.directionId) }
					)
				}
			} ?: Result.Error(null)
	}
}