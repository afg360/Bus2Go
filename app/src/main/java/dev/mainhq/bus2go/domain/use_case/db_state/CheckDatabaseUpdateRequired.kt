package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.repository.TransitRepository
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class CheckDatabaseUpdateRequired(
	private val appStateRepository: AppStateRepository,
	private val transitRepos: List<TransitRepository>,
) {

	/** @return If any error or no [TransitRepository] available, returns a [Flow] of a [Result.Error]. Otherwise,
	 * returns a [Result.Success] of a list of the [DatabaseAgency] to be updated (empty if no update required). */
	operator fun invoke(): Flow<Result<List<DatabaseAgency>>> {
		return combine(
			transitRepos.map { repo -> repo.databaseExpirationDate.map { repo to it } }
		) { pairs ->
			if (pairs.isEmpty()) {
				listOf(null to Result.Error(null, "None of the databases exist..."))
			}
			else {
				pairs.filter {
					when(val date = it.second) {
						is Result.Success<LocalDate> -> {
							date.data <= LocalDate.now()
						}
						is Result.Error -> {
							false
						}
					}
				}
			}
		}.map { list ->
			if (list.isEmpty()) {
				Result.Success(list)
			}
			if (list.any { it.first == null }) {
				Result.Error(null, null)
			}
			else {
				Result.Success(
					list.map { pair ->
						pair.first!!.transitType.toDatabaseAgency()
					}
				)
			}
		}
	}

}