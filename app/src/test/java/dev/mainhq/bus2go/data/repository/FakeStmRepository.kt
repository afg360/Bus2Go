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
import dev.mainhq.bus2go.utils.FuzzyQuery
import java.time.LocalDate
import kotlin.random.Random

class FakeStmRepository: StmRepository {

	val stmRouteInfo =  listOf(
		StmBusRouteInfo(
			230.toString(),
			"STM BUS 230"
		),
		StmBusRouteInfo(
			1.toString(),
			"Un metro STN... ligne 1"
		),
		//this shouldnt be allowed
		StmBusRouteInfo(
			"SomeText",
			"STM error should happen"
		)
	)

	val stmTransitData = listOf(
		StmBusItem(
			"192",
			"Some First stopName",
			"Est",
			0,
			"LastStop written"
		),
		StmBusItem(
			"192",
			"Second stopName",
			"Est",
			0,
			"LastStop written"
		),
		StmBusItem(
			"192",
			"Last stopName",
			"Est",
			0,
			"LastStop written"
		),
		StmBusItem(
			"192",
			"Some First Other stopName",
			"Ouest",
			1,
			"LastStop written"
		),
		StmBusItem(
			"192",
			"Second Other stopName",
			"Ouest",
			1,
			"LastStop written"
		),
		StmBusItem(
			"192",
			"Last Other stopName",
			"Ouest",
			1,
			"Last Other written"
		)
	)

	val stopTimes = hashMapOf<StmBusItem, Time>()

	init {
		stmTransitData.forEach {
			stopTimes[it] = Time.fromUnix(Random.nextLong(0, Long.MAX_VALUE))
		}
	}

	override suspend fun getMaxEndDate(): LocalDate? {
		return stopTimes.map { it.value }.maxByOrNull { it }!!.localDate
	}

	override suspend fun getAllCalendarDates(): List<CalendarDates> {
		TODO("Not yet implemented")
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
		TODO("Not yet implemented")
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

	override suspend fun getDirectionInfo(routeId: Int): List<DirectionInfo> {
		return stmTransitData.filter { it.routeId.toInt() == routeId }
			.map { DirectionInfo(it.direction, it.directionId)
		}
	}
}