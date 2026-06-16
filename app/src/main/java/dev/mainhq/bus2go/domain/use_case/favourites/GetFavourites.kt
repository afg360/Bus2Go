package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.FavouritesPositionRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFavourites(
	private val stmFavouritesRepository: StmFavouritesRepository,
	private val exoFavouritesRepository: ExoFavouritesRepository,
) {

	operator fun invoke(): Flow<HashMap<TransitType, List<FavouriteTransitData>>> {
		return combine(
			stmFavouritesRepository.getStmBusFavourites(),
			exoFavouritesRepository.getExoBusFavourites(),
			exoFavouritesRepository.getExoTrainFavourites(),
		) { stm, exo, exoTrain ->
			hashMapOf(
				TransitType.STM to stm,
				TransitType.EXO_BUS to exo,
				TransitType.EXO_TRAIN to exoTrain
			)
		}
	}
}