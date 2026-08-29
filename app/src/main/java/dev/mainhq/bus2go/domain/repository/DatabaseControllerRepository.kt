package dev.mainhq.bus2go.domain.repository

import android.content.Context
import dev.mainhq.bus2go.domain.core.Bus2GoLocalDatabase
import dev.mainhq.bus2go.domain.entity.DatabaseAgency

interface DatabaseControllerRepository {

	fun createInstance(context: Context, databaseAgency: DatabaseAgency): Bus2GoLocalDatabase?

	fun deleteDatabase(databaseAgency: DatabaseAgency): Boolean
}