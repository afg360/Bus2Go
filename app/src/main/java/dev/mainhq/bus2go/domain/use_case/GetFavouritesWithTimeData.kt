package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.utils.Time
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.coroutineContext

//should not only take favourites but also the time
//(and since the time is periodically calculated, perhaps it should be sent as a flow instead?
class GetFavouritesWithTimeData(
	private val exoFavouritesRepo: ExoFavouritesRepository,
	private val stmFavouritesRepository: StmFavouritesRepository,
	private val stmRepository: StmRepository,
	private val exoRepository: ExoRepository
) {

	//FIXME perhaps use a flow instead since we will be continusously updating the curTime
	suspend operator fun invoke(): List<FavouriteTransitDataWithTime>{
		val time = Time.now()
		val stmBusFavourites = stmFavouritesRepository.getStmBusFavourites().map {
			stmRepository.getFavouriteStopTime(it, time)
		}
		val exoBusFavourites = exoFavouritesRepo.getExoBusFavourites().map {
			exoRepository.getFavouriteBusStopTime(it, time)
		}
		val exoTrainFavourites = exoFavouritesRepo.getExoTrainFavourites().map {
			exoRepository.getFavouriteTrainStopTime(it, time)
		}
		return stmBusFavourites + exoBusFavourites + exoTrainFavourites
	}
}
