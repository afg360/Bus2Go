package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import java.time.LocalDate

interface TransitRepository {
	val dbName: String

	/** @return Latest calendar date before data not being up to date. **/
	suspend fun getDatabaseExpirationDate(): Result<LocalDate>
}