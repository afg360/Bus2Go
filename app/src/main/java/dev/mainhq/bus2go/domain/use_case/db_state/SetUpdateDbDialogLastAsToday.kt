package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.repository.AppStateRepository

class SetUpdateDbDialogLastAsToday(
	private val appStateRepository: AppStateRepository
) {
	suspend operator fun invoke(){
		appStateRepository.setUpdateDbDialogLastShownDate(Time.now())
	}
}