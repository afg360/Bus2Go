package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.StmBusItem
import java.time.LocalDate
import kotlin.random.Random

class FakeExoRepository: ExoRepository {

	private val exoBusRouteIds = listOf(
		230.toString(),
		1.toString(),
		"T102",
		192.toString()
	)

	private val exoBusRouteInfo = exoBusRouteIds.map { ExoBusRouteInfo(it, "EXO BUS $it") }

	val exoBusTransitData = exoBusRouteInfo.map {
		val list = mutableListOf<ExoBusItem>()
		for (i in 1..5)
			list.add(ExoBusItem(it.routeId, "StopName $i", "Est", "Route Long Name 1", "StopName 5"))

		for (i in 5 downTo 1)
			list.add(ExoBusItem(it.routeId, "StopName $i", "Ouest", "Route Long Name 2 REVERSE", "StopName 1 (Last Other)"))
		list
	}.flatten()


	private val exoTrainRouteInfo = (1..5).map { ExoTrainRouteInfo(it.toString(), "EXO TRAIN $it", it) }

	private val exoTrainTransitData = exoTrainRouteInfo.map{
		val list = mutableListOf<ExoTrainItem>()
		for (i in 1..5)
			list.add(ExoTrainItem(it.routeId, "StopName $i", "Est", it.trainNum, "Route Long Name 1", 0))

		for (i in 5 downTo 1)
			list.add(ExoTrainItem(it.routeId, "StopName $i", "Ouest", it.trainNum, "Route Long Name 2 REVERSE", 1))
		list
	}.flatten()


	val stopTimesBus = hashMapOf<ExoBusItem, Time>()
	val stopTimesTrain = hashMapOf<ExoTrainItem, Time>()

	init {
		exoBusTransitData.forEach {
			stopTimesBus[it] = Time.fromUnix(Random.nextLong(0, Int.MAX_VALUE.toLong()))
		}
		exoTrainTransitData.forEach {
			stopTimesTrain[it] = Time.fromUnix(Random.nextLong(0, Int.MAX_VALUE.toLong()))
		}
	}

	override suspend fun getMaxEndDate(): LocalDate? {
		return LocalDate.now()
	}


	override suspend fun getRouteInfo(routeId: FuzzyQuery): List<RouteInfo> {
		//create a list of arbitrary routeInfos
		//then return a subset of the list that respect the query
		return (exoBusRouteInfo + exoTrainRouteInfo).filter {
			it.routeId.contains(routeId.query) || it.routeName.contains(routeId.query)
		}
	}

	override suspend fun getStopNames(directions: Pair<String, String>): Pair<List<String>, List<String>> {
		return Pair(
			exoBusTransitData.filter { it.direction == directions.first }.map { it.stopName },
			exoBusTransitData.filter { it.direction == directions.second }.map { it.stopName }
		)
	}

	override suspend fun getTrainStopNames(routeId: String): Pair<List<String>, List<String>> {
		return Pair(
			exoTrainTransitData.filter { it.routeId == routeId }.map { it.stopName },
			exoTrainTransitData.filter { it.routeId == routeId }.map { it.stopName }
		)
	}

	override suspend fun getStopTimes(exoBusItem: ExoBusItem, curTime: Time): List<Time> {
		return stopTimesBus.filter { it.key == exoBusItem && it.value > curTime }
			.map { it.value }
	}

	override suspend fun getOldStopTimes(exoTransitData: TransitData, curTime: Time): List<Time> {
		throw IllegalStateException("Not implemented")
	}

	override suspend fun getFavouriteBusStopTime(
		exoFavouriteBusItem: ExoBusItem,
		curTime: Time,
	): TransitDataWithTime {
		return stopTimesBus.filter { it.key == exoFavouriteBusItem && it.value > curTime }
			.map { TransitDataWithTime(it.key, it.value) }
			.first()

	}

	override suspend fun getTrainStopTimes(exoTrainItem: ExoTrainItem, curTime: Time): List<Time> {
		return stopTimesTrain.filter { it.key == exoTrainItem && it.value > curTime }
			.map { it.value }
	}

	override suspend fun getFavouriteTrainStopTime(
		exoFavouriteTrainItem: ExoTrainItem,
		curTime: Time,
	): TransitDataWithTime {
		return stopTimesTrain.filter { it.key == exoFavouriteTrainItem && it.value > curTime }
			.map { TransitDataWithTime(it.key, it.value) }
			.first()
	}

	override suspend fun getTripHeadsigns(routeId: String): List<String> {
		return exoBusTransitData.filter { it.routeId == routeId }
					.map { it.headsign } +
				exoTrainTransitData.filter { it.routeId == routeId }
					.map { it.direction }
	}
}