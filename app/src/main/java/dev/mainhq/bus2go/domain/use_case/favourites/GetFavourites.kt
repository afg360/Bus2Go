package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.ExoFavouritesRepository
import dev.mainhq.bus2go.domain.repository.StmFavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFavourites(
	private val exoFavouritesRepository: ExoFavouritesRepository,
	private val stmFavouritesRepository: StmFavouritesRepository
) {

	operator fun invoke(): Flow<HashMap<TransitType, List<TransitData>>> {
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