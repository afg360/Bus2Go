package app.bus2go.android.domain.use_case.db_state

import app.bus2go.android.domain.core.Result
import app.bus2go.android.domain.repository.AppStateRepository
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