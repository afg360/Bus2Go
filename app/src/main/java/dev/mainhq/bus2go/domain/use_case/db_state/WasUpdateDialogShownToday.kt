package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import java.time.LocalDate

class WasUpdateDialogShownToday(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(): Boolean {
		return when(val response = appStateRepository.getDbUpdateDialogLastShownDate()){
			is Result.Error -> false
			is Result.Success<LocalDate> -> response.data >= LocalDate.now()
		}

	}
}