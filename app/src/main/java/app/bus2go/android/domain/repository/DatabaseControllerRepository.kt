package app.bus2go.android.domain.repository

import app.bus2go.android.domain.core.Bus2GoLocalDatabase
import app.bus2go.android.domain.entity.DatabaseAgency
import kotlinx.coroutines.flow.StateFlow

interface DatabaseControllerRepository {

	fun getDatabaseFlow(databaseAgency: DatabaseAgency): StateFlow<Bus2GoLocalDatabase?>

	fun deleteDatabase(databaseAgency: DatabaseAgency): Boolean
}