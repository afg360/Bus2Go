package app.bus2go.android.domain.use_case.db_state

import app.bus2go.android.domain.core.Result
import app.bus2go.android.domain.entity.TransitData
import app.bus2go.android.domain.repository.TransitRepository
import app.bus2go.android.utils.queryRepos
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