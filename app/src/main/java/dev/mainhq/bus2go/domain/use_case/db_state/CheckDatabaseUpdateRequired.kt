package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.use_case.transit.GetMinDateForUpdate
import java.time.LocalDate

class CheckDatabaseUpdateRequired(
	private val appStateRepository: AppStateRepository,
	private val setDatabaseState: SetDatabaseState,
	private val getMinDateForUpdate: GetMinDateForUpdate
) {

	/**
	 * @return If it returns Failure, then need to display some sort of message to the user to answer
	 **/
	suspend operator fun invoke(): LocalDate? {
		val state = appStateRepository.getDatabaseState()
		if (state == null){
			val minDate = getMinDateForUpdate()
			if (minDate == null) return null
			else setDatabaseState(minDate)
			return minDate
		}
		else {
			return if (state < LocalDate.now()) null
			else state
		}
	}

}