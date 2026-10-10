package app.bus2go.android.domain.use_case.db_state

import app.bus2go.android.domain.entity.Time
import app.bus2go.android.domain.repository.AppStateRepository

class SetUpdateDbDialogLastAsToday(
	private val appStateRepository: AppStateRepository
) {
	suspend operator fun invoke(){
		appStateRepository.setUpdateDbDialogLastShownDate(Time.now())
	}
}