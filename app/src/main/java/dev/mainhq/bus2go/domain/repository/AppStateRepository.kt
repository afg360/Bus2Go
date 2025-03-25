package dev.mainhq.bus2go.domain.repository

import java.time.LocalDate

interface AppStateRepository {

	/** Retrieves the expiration date of databases stored for the app. **/
	suspend fun getDatabaseState(): LocalDate?
	suspend fun setDatabaseState(localDate: LocalDate)

	/** Checks if it is the first time that the app has been launched. **/
	suspend fun isFirstTime(): Boolean
}