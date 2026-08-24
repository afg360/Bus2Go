package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository
import dev.mainhq.bus2go.domain.repository.TransitRepository
import dev.mainhq.bus2go.utils.queryRepos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest

class GetFavouritesWithTimeData(
	private val transitRepos: List<TransitRepository>,
	private val getFavourites: GetFavourites,
	private val favouritesPosition: FavouritesPositionRepository
) {

	@OptIn(ExperimentalCoroutinesApi::class)
	operator fun invoke(): Flow<Result<List<FavouriteTransitDataWithTime>>> {
		return getFavourites.invoke().transformLatest { map ->
			while (true) {
				val time = Time.now()
				emit(map.map { map ->
					when(map.key) {
						TransitType.STM -> {
							map.value
								.mapNotNull {
									(transitRepos.queryRepos(TransitType.STM)
										.getFavouriteStopTime(it, time)
									as? Result.Success)?.data
								}
						}
						TransitType.EXO_BUS -> {
							map.value
								.mapNotNull {
									(transitRepos.queryRepos(TransitType.EXO_BUS)
										.getFavouriteStopTime(it, time)
									as? Result.Success)?.data
								}
						}
						TransitType.EXO_TRAIN -> {
							map.value.mapNotNull {
								(transitRepos.queryRepos(TransitType.EXO_TRAIN)
									.getFavouriteStopTime(it, time)
										as? Result.Success)
									?.data
							}
						}
					}
				}.flatten())
				delay(5000)
			}
		}.combine(favouritesPosition.getFavouritesPositions()) { favourites, positions ->
			Result.Success(
				data = positions.map { position ->
					//FIXME must do this null check, since when deleting, positions may exist at the
					// moment when favourites don't anymore which could cause a NullPointerException
					return@map favourites.find { it.favouriteTransitData.id.toString() == position.itemId }
						?: return@combine Result.Error(null)
				}
			)
		}
	}
}
