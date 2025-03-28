package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.domain.entity.Time
import java.time.LocalDate

class GetMinDateForUpdate(
	private val exoRepository: ExoRepository,
	private val stmRepository: StmRepository
) {

	/**
	 * @return The closest date before one of the databases gets out of date
	 **/
	suspend operator fun invoke(): LocalDate? {
		val exo = exoRepository.getMaxEndDate() ?: return null
		val stm = stmRepository.getMaxEndDate() ?: return null
		val curDate = Time.now()
		val duration1 = Time(exo).minusDays(curDate) ?: return null
		val duration2 = Time(stm).minusDays(curDate) ?: return null
		return if (duration1 < duration2) exo else stm
	}
}