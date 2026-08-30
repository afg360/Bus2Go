package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import java.time.LocalDate

class SetDatabaseExpirationDate(
	private val appStateRepository: AppStateRepository
) {

	suspend operator fun invoke(time: Time){
		appStateRepository.setNextDatabaseExpirationNotifDate(time)
	}
}