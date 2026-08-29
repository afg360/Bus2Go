package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.repository.TransitRepository
import dev.mainhq.bus2go.utils.queryRepos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class GetDatabaseExpiryDate(
	private val transitRepos: List<TransitRepository>
){

	operator fun invoke(transitData: TransitData): Flow<LocalDate> {
		return transitRepos.queryRepos(transitData.transitType).databaseExpirationDate.map {
			when(it) {
				is Result.Error -> throw IllegalStateException("Cannot search for a transit data that doesn't exist")
				is Result.Success<LocalDate> -> it.data
			}
		}
	}
}