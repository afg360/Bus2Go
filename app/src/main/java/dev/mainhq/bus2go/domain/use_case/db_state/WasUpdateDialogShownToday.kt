package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.repository.TransitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WasUpdateDialogShownToday(
	private val appStateRepository: AppStateRepository
) {

	operator fun invoke(): Flow<Boolean> {
		return appStateRepository.getDbUpdateDialogLastShownDate().map {
			when (it) {
				is Result.Error -> false
				is Result.Success<LocalDate> -> it.data >= LocalDate.now()
			}

		}
	}
}