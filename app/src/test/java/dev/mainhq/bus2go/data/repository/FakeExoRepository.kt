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
import dev.mainhq.bus2go.utils.FuzzyQuery
import java.time.LocalDate
import kotlin.random.Random

class FakeExoRepository: ExoRepository {

	private val exoBusRouteInfo = listOf(
		ExoBusRouteInfo(
			230.toString(),
			"EXO BUS 230"
		),
		ExoBusRouteInfo(
			124.toString(),
			"EXO BUS 124"
		),
		ExoBusRouteInfo(
			"T140",
			"EXO BUS T140"
		)
	)

	val exoBusTransitData = listOf(
		ExoBusItem(
			"192",
			"Some First stopName",
			"Est",
			"Long name 0",
			"LastStop written"
		),
		ExoBusItem(
			"192",
			"Second stopName",
			"Est",
			"Long name 1",
			"LastStop written"
		),
		ExoBusItem(
			"192",
			"Last stopName",
			"Est",
			"Long name 2",
			"LastStop written"
		),
		ExoBusItem(
			"192",
			"Some First Other stopName",
			"Ouest",
			"Long name 3",
			"LastStop written"
		),
		ExoBusItem(
			"192",
			"Second Other stopName",
			"Ouest",
			"Long name 3",
			"LastStop written"
		),
		ExoBusItem(
			"192",
			"Last Other stopName",
			"Ouest",
			"Long name 4",
			"Last Other written"
		)
	)

	private val exoTrainRouteInfo = listOf(
		ExoTrainRouteInfo(
			230.toString(),
			"EXO TRAIN 230",
			1
		),
		ExoTrainRouteInfo(
			124.toString(),
			"EXO TRAIN 124",
			5
		),
		ExoTrainRouteInfo(
			10.toString(),
			"EXO TRAIN 10",
			3
		)
	)

	private val exoTrainTransitData = listOf(
		ExoTrainItem(
			"192",
			"Some First stopName",
			"Est",
			1,
			"LastStop written",
			directionId = 0
		),
		ExoTrainItem(
			"192",
			"Second stopName",
			"Est",
			1,
			"LastStop written",
			directionId = 0
		),
		ExoTrainItem(
			"192",
			"Last stopName",
			"Est",
			1,
			"LastStop written",
			directionId = 0
		),
		ExoTrainItem(
			"192",
			"Some First Other stopName",
			"Ouest",
			1,
			"LastStop written",
			directionId = 1
		),
		ExoTrainItem(
			"192",
			"Second Other stopName",
			"Ouest",
			1,
			"LastStop written",
			directionId = 1
		),
		ExoTrainItem(
			"192",
			"Last Other stopName",
			"Ouest",
			1,
			"Last Other written",
			directionId = 1
		)
	)

	val stopTimesBus = hashMapOf<ExoBusItem, Time>()
	val stopTimesTrain = hashMapOf<ExoTrainItem, Time>()

	init {
		exoBusTransitData.forEach {
			stopTimesBus[it] = Time.fromUnix(Random.nextLong(0, Long.MAX_VALUE))
		}
		exoTrainTransitData.forEach {
			stopTimesTrain[it] = Time.fromUnix(Random.nextLong(0, Long.MAX_VALUE))
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
		TODO("Not yet implemented")
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