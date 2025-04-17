package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.domain.entity.Time
import io.ktor.util.reflect.instanceOf

//should not only take favourites but also the time
//(and since the time is periodically calculated, perhaps it should be sent as a flow instead?
class GetFavouritesWithTimeData(
	private val exoFavouritesRepo: ExoFavouritesRepository,
	private val exoRepository: ExoRepository,
	private val stmFavouritesRepository: StmFavouritesRepository,
	private val stmRepository: StmRepository,
) {

	//FIXME perhaps use a flow instead since we will be continusously updating the curTime
	suspend operator fun invoke(): List<TransitDataWithTime>{
		val time = Time.now()
		//TODO some cleanup to be able to actually deal with failures
		//before the filter, if we are empty we are chill. else, if we are empty after, we must notify
		val stmBusFavourites = stmFavouritesRepository.getStmBusFavourites().map {
			stmRepository.getFavouriteStopTime(it, time)
		}.filter { it.instanceOf(Result.Success::class) }
			.map { (it as Result.Success).data }

		val exoBusFavourites = exoFavouritesRepo.getExoBusFavourites().map {
			exoRepository.getFavouriteBusStopTime(it, time)
		}.filter { it.instanceOf(Result.Success::class) }
			.map { (it as Result.Success).data }

		val exoTrainFavourites = exoFavouritesRepo.getExoTrainFavourites().map {
			exoRepository.getFavouriteTrainStopTime(it, time)
		}.filter { it.instanceOf(Result.Success::class) }
			.map { (it as Result.Success).data }

		return stmBusFavourites + exoBusFavourites + exoTrainFavourites
	}
}
