package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.core.Result
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
	suspend operator fun invoke(): Result<LocalDate?> {
		val state = appStateRepository.getDatabaseState()
		if (state == null){
			when(val minDate = getMinDateForUpdate.invoke()){
				is Result.Error -> return minDate
				is Result.Success<LocalDate?> -> {
					if (minDate.data == null) return Result.Success(null)
					setDatabaseState.invoke(minDate.data)
					return minDate
				}
			}
		}
		else {
			return if (state < LocalDate.now()) Result.Success(null)
			else Result.Success(state)
		}
	}

}