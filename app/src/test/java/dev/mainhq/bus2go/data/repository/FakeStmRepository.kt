package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.entity.stm.CalendarDates
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import java.time.LocalDate
import java.time.LocalTime

class FakeStmRepository: StmRepository {

	private val stmRouteIds = listOf(
		230.toString(),
		1.toString(), //"metro"
		"SomeText", //"wrong" shit
		192.toString()
	)

	val stmRouteInfo: List<StmBusRouteInfo> = stmRouteIds.map {
		if (it.all { it.isDigit() }){
			if (it.toInt() > 5) StmBusRouteInfo(it, "STM BUS $it")
			else StmBusRouteInfo(it, "Un metro STM... ligne $it")
		}
		else StmBusRouteInfo(it, "STM error should happen")
	}

	val stmTransitData = stmRouteInfo.map {
		val list = mutableListOf<StmBusItem>()
		for (i in 1..5)
			list.add(StmBusItem(it.routeId, "StopName $i", "Est", 0, "StopName 5"))

		for (i in 5 downTo 1)
			list.add(StmBusItem(it.routeId, "StopName $i", "Ouest", 1, "StopName 1"))
		list
	}.flatten()


	val testDate = LocalDate.of(2025, 2, 4)
	val stopTimes = hashMapOf<StmBusItem, List<Time>>()

	init {
		stmTransitData.forEachIndexed { index, item ->
			val hour = 8 + (index / 2) % 12
			val minute = (index % 4) * 15
			val time = Time(testDate, LocalTime.of(hour, minute, 0))
			stopTimes[item] = (1..10)
				.map { Time(testDate, LocalTime.of(hour, minute + it, 0)) }
		}
	}

	override suspend fun getMaxEndDate(): Result<LocalDate> {
		return stopTimes.map { stopTime -> stopTime.value.maxByOrNull { it.localDate } }
			.filterNotNull().maxByOrNull { it }
			?.localDate?.let { Result.Success(it) } ?: Result.Error(null)
	}

	override suspend fun getAllCalendarDates(): Result<List<CalendarDates>> {
		throw IllegalStateException("Not implemented")
	}


	override suspend fun getBusRouteInfo(routeId: FuzzyQuery): Result<List<RouteInfo>> {
		return Result.Success(stmRouteInfo.filter { it.routeId.contains(routeId.query) || it.routeName.contains(routeId.query) })
	}

	override suspend fun getStopName(stopId: Int): Result<String> {
		if (stopId >= stmTransitData.size){
			throw IllegalArgumentException("The fake stm repo only contains ${stmTransitData.size} entries...")
		}
		return Result.Success(stmTransitData[stopId].stopName)
	}

	override suspend fun getStopNames(
		headsign1: String,
		headsign2: String,
		routeId: String,
	): Result<Pair<List<String>, List<String>>> {
		return Result.Success(Pair(
			stmTransitData.filter { it.routeId == routeId && it.direction == headsign1 }
				.map { it.stopName },
			stmTransitData.filter { it.routeId == routeId && it.direction == headsign2 }
				.map { it.stopName }
		))
	}

	override suspend fun getOldTimes(stmTransitData: TransitData, curTime: Time): Result<List<Time>> {
		throw IllegalStateException("Not implemented")
	}

	override suspend fun getStopTimes(stmTransitData: TransitData, curTime: Time): Result<List<Time>> {
		return Result.Success(stopTimes[stmTransitData]
			?.filter { it > curTime }
			?.map { Time((it - curTime)!!) }
			?: listOf()
		)
	}

	/*
	override suspend fun getStopTimes(
		stopName: String,
		headsign: String,
		routeId: Int,
		curTime: Time,
	): List<Time> {
		return stopTimes[StmBusItem(routeId, stopName, getDirectionInfo(routeId)[0].directionId, )]
	}
	 */

	override suspend fun getFavouriteStopTime(
		stmFavouriteBusItem: StmBusItem,
		curTime: Time,
	): Result<TransitDataWithTime> {
		return Result.Success(TransitDataWithTime(stmFavouriteBusItem, stopTimes[stmFavouriteBusItem]?.first()))
	}

	/**
	 * @throws NumberFormatException If the stored routeId contains some wrongly formatted routeId
	 **/
	override suspend fun getDirectionInfo(routeId: Int): Result<List<DirectionInfo>> {
		return Result.Success(stmTransitData
			.filter { it.routeId.all { it.isDigit() } }
			.filter { it.routeId.toInt() == routeId }
			.map { DirectionInfo.StmDirectionInfo(it.direction, it.directionId) }
		)
	}
}