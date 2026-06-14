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
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

//should not only take favourites but also the time
//(and since the time is periodically calculated, perhaps it should be sent as a flow instead?
class GetFavouritesWithTimeData(
	private val getFavourites: GetFavourites,
	private val exoRepository: ExoRepository,
	private val stmRepository: StmRepository,
) {

	//FIXME perhaps use a flow instead since we will be continusously updating the curTime
	@OptIn(ExperimentalCoroutinesApi::class)
	operator fun invoke(): Flow<Result<List<FavouriteTransitDataWithTime>>> {
		//TODO have a better algorithm to handle correctly the position sorting (right now STM will always be on top of others)
		return getFavourites.invoke().transformLatest { hashMap ->
			while (true) {
				val time = Time.now()
				emit(Result.Success(
					data = hashMap.map { map ->
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
					//FIXME what happens if multiple items have the same position number (e.g. because from different agencies)
				))
				delay(5000)
			}
		}
	}
}
