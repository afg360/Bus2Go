package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Bus2GoLocalDatabase
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import kotlinx.coroutines.flow.StateFlow

interface DatabaseControllerRepository {

	fun getDatabaseFlow(databaseAgency: DatabaseAgency): StateFlow<Bus2GoLocalDatabase?>

	fun deleteDatabase(databaseAgency: DatabaseAgency): Boolean
}