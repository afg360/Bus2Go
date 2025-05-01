package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.Time
import java.time.LocalDate

class GetMinDateForUpdate(
	private val exoRepository: ExoRepository,
	private val stmRepository: StmRepository
) {

	/**
	 * @return The closest date before one of the databases gets out of date
	 **/
	suspend operator fun invoke(): Result<LocalDate?> {
		val exo = exoRepository.getMaxEndDate()
		val stm = stmRepository.getMaxEndDate()
		if (exo is Result.Error && stm is Result.Error){
			//FIXME deal with both throwables, not only one of them...
			return Result.Error(exo.throwable, "Both databases don't exist...")
		}

		if (exo is Result.Error) return stm
		else if (stm is Result.Error) return exo
		else {
			val curDate = Time.now()
			val duration1 = Time((exo as Result.Success).data).minusDays(curDate) ?: return Result.Success(null)
			val duration2 = Time((stm as Result.Success).data).minusDays(curDate) ?: return Result.Success(null)
			return if (duration1 < duration2) exo else stm
		}
	}
}