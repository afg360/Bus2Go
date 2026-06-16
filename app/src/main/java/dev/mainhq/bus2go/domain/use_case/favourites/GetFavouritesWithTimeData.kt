package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.StmBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoBusFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData.ExoTrainFavouriteItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest

//should not only take favourites but also the time
//(and since the time is periodically calculated, perhaps it should be sent as a flow instead?
class GetFavouritesWithTimeData(
	private val getFavourites: GetFavourites,
	private val stmRepository: StmRepository,
	private val exoRepository: ExoRepository,
	private val favouritesPosition: FavouritesPositionRepository
) {

	@OptIn(ExperimentalCoroutinesApi::class)
	operator fun invoke(): Flow<Result<List<FavouriteTransitDataWithTime>>> {
		return getFavourites.invoke().transformLatest { hashMap ->
			while (true) {
				val time = Time.now()
				emit(hashMap.map { map ->
						when(map.key) {
							TransitType.STM -> {
								map.value
									.map { stmRepository.getFavouriteStopTime(it as StmBusFavouriteItem, time) }
									.filter { it.instanceOf(Result.Success::class) }
									.map { (it as Result.Success).data }
							}
							TransitType.EXO_BUS -> {
								map.value
									.map { exoRepository.getFavouriteBusStopTime(it as ExoBusFavouriteItem, time) }
									.filter { it.instanceOf(Result.Success::class) }
									.map { (it as Result.Success).data }
							}
							TransitType.EXO_TRAIN -> {
								map.value
									.map { exoRepository.getFavouriteTrainStopTime(it as ExoTrainFavouriteItem, time) }
									.filter { it.instanceOf(Result.Success::class) }
									.map { (it as Result.Success).data }
							}
						}
					}.flatten()
				)
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
