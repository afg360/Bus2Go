package dev.mainhq.bus2go.domain.repository

import java.time.LocalDate

//TODO output a result object (in case of failure, we will retry...), or maybe a boolean...
interface AppStateRepository {

	/** Retrieves the expiration date of databases stored for the app. **/
	suspend fun getDatabaseState(): LocalDate?
	suspend fun setDatabaseState(localDate: LocalDate)

	/** Gets the version of the saved local STM database */
	suspend fun getStmDatabaseVersion(): Int

	/** Gets the version of the saved local EXO database */
	suspend fun getExoDatabaseVersion(): Int

	/** Checks if it is the first time that the app has been launched. **/
	suspend fun getIsFirstTime(): Boolean

	/** Initialises the isFirstTime flag. */
	suspend fun setIsFirstTime()
}