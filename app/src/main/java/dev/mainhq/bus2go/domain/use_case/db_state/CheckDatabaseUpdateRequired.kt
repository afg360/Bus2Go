package dev.mainhq.bus2go.domain.use_case.db_state

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.repository.TransitRepository
import java.time.LocalDate

class CheckDatabaseUpdateRequired(
	private val appStateRepository: AppStateRepository,
	private val setDatabaseExpirationDate: SetDatabaseExpirationDate,
	private val transitRepos: List<TransitRepository>,
) {

	/** @return May be null if the time has already passed. Else the database is up to date at the moment */
	suspend operator fun invoke(): Result<List<DbToDownload>> {
		val minDatesForUpdate = transitRepos.map { it.dbName to it.getMaxEndDate() }
		if (minDatesForUpdate.isEmpty()) {
			return Result.Error(null, "None of the databases exist...")
		}
		else if (minDatesForUpdate.all { it.second is Result.Error }){
			//FIXME deal with both throwables, not only one of them...
			return Result.Error(null, "Query error on all databases")
		}

		return Result.Success(minDatesForUpdate.filter { it.second is Result.Success<LocalDate> }
			.filter { (it.second as Result.Success<LocalDate>).data <= LocalDate.now() }
			.map {
				when(it.first.lowercase()) {
					"stm" -> DbToDownload.STM
					"exo" -> DbToDownload.EXO
					else -> throw IllegalStateException("Invalid data")
				}
			}
		)

		/*
		//TODO do not store the result bcz of bug in commented thing, but need a way to differentiate
		// for every dbs without relying on different classes in shit when we will get a ton of
		// different ones in the future
		return when(val minDate = getMinDateForUpdate.invoke()){
			//if no db downloaded, an error is sent
			is Result.Error -> minDate
			//null if the time has passed already
			is Result.Success<LocalDate?> -> {
				if (minDate.data == null) {
					Result.Success(null)
				}
				else {
					if (minDate.data <= LocalDate.now()) {
						Result.Success(null)
					}
					else {
						minDate
					}
				}
			}
		}
		 */
		/*
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
							//FIXME this is wrong, since it only writes for one of the databases
							// if both are used, but the first one installed or whatnot is not set,
							// only one of them will be accurately depicted
							// e.g. stm is expired, exo is still good, but exo was downloaded first
							// -> will show as if all is up to date bcz exo expiry date will be shown
							// could make it a query directly to db instead... but annoying to then test and shit...
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

		 */
	}

}