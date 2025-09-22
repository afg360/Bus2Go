package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.use_case.transit.GetMinDateForUpdate
import java.time.LocalDate

class CheckDatabaseUpdateRequired(
	private val appStateRepository: AppStateRepository,
	private val setDatabaseExpirationDate: SetDatabaseExpirationDate,
	private val getMinDateForUpdate: GetMinDateForUpdate
) {

	/** @return May be null if the time has already passed. Else the database is up to date at the moment */
	suspend operator fun invoke(): Result<LocalDate?> {
		val response = appStateRepository.getDatabaseExpirationDate()
		when (response){
			is Result.Error -> {
				//if nothing was found in the file, query the database to check for the minimum date
				return when(val minDate = getMinDateForUpdate.invoke()){
					//if no db downloaded, an error is sent
					is Result.Error -> minDate
					//null if the time has passed already
					is Result.Success<LocalDate?> -> {
						if (minDate.data == null) {
							Result.Success(null)
						}
						else {
							//we write the value in the file if it is not null so that we don't have to query
							// the db again
							setDatabaseExpirationDate.invoke(minDate.data)
							if (minDate.data <= LocalDate.now()) {
								Result.Success(null)
							}
							else {
								minDate
							}
						}
					}
				}
			}
			is Result.Success<LocalDate> -> {
				return if (response.data <= LocalDate.now()) {
					Result.Success(null)
				}
				else {
					Result.Success(response.data)
				}
			}
		}
	}

}