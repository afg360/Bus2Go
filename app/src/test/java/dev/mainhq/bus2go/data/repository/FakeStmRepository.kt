package dev.mainhq.bus2go.data.repository

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
import kotlin.random.Random

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


	val stopTimes = hashMapOf<StmBusItem, Time>()

	init {
		stmTransitData.forEach {
			stopTimes[it] = Time.fromUnix(Random.nextLong(0, Int.MAX_VALUE.toLong()))
		}
	}

	override suspend fun getMaxEndDate(): LocalDate? {
		return stopTimes.map { it.value }.maxByOrNull { it }!!.localDate
	}

	override suspend fun getAllCalendarDates(): List<CalendarDates> {
		throw IllegalStateException("Not implemented")
	}


	override suspend fun getBusRouteInfo(routeId: FuzzyQuery): List<RouteInfo> {
		return stmRouteInfo.filter { it.routeId.contains(routeId.query) || it.routeName.contains(routeId.query) }
	}

	override suspend fun getStopName(stopId: Int): String {
		if (stopId >= stmTransitData.size){
			throw IllegalArgumentException("The fake stm repo only contains ${stmTransitData.size} entries...")
		}
		return stmTransitData[stopId].stopName
	}

	override suspend fun getStopNames(
		headsigns: Pair<String, String>,
		routeId: String,
	): Pair<List<String>, List<String>> {
		return Pair(
			stmTransitData.filter { it.routeId == routeId && it.direction == headsigns.first }
				.map { it.stopName },
			stmTransitData.filter { it.routeId == routeId && it.direction == headsigns.second }
				.map { it.stopName }
		)
	}

	override suspend fun getOldTimes(stmTransitData: TransitData, curTime: Time): List<Time> {
		throw IllegalStateException("Not implemented")
	}

	override suspend fun getStopTimes(stmTransitData: TransitData, curTime: Time): List<Time> {
		return stopTimes
			.filter { it.key == stmTransitData && it.value > curTime }
			.map { it.value }
			.map { Time((it - curTime)!!) }
	}

	override suspend fun getStopTimes(
		stopName: String,
		headsign: String,
		routeId: Int,
		curTime: Time,
	): List<Time> {
		return stopTimes.map { it.value }
	}

	override suspend fun getFavouriteStopTime(
		stmFavouriteBusItem: StmBusItem,
		curTime: Time,
	): TransitDataWithTime {
		return stopTimes.filter { it.key == stmFavouriteBusItem }
			.map { TransitDataWithTime(it.key, it.value) }
			.first()
	}

	/**
	 * @throws NumberFormatException If the stored routeId contains some wrongly formatted routeId
	 **/
	override suspend fun getDirectionInfo(routeId: Int): List<DirectionInfo> {
		return stmTransitData.filter { it.routeId.toInt() == routeId }
			.map { DirectionInfo(it.direction, it.directionId)
		}
	}
}