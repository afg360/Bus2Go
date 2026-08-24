package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.DatabaseState
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.Progress
import kotlinx.coroutines.flow.Flow
import java.security.cert.X509Certificate
import java.time.LocalDate

//TODO output a result object (in case of failure, we will retry...), or maybe a boolean...
interface AppStateRepository {

	/**
	 * Retrieves the sooner expiration date of databases stored for the app.
	 * Could also represent the date when the user would like to next receive some sort of notification
	 **/
	suspend fun getNextDatabaseExpirationNotifDate(): Result<LocalDate>
	suspend fun setNextDatabaseExpirationNotifDate(localDate: LocalDate)

	/** Gives a list of garbage files downloaded by the application (old databases, part files, etc.) */
	suspend fun getGarbageFiles(): List<String>

	//FIXME change from DbToDownload to some other enum name
	/**
	 * @return Whether the version of the database file of the given data exists.
	 * @throws IllegalArgumentException When giving ALL instead of a single db type.
	 * */
	@Throws(IllegalArgumentException::class)
	suspend fun doesUpToDateCompressedDbExist(db: DatabaseAgency, version: Int): String?

	/** @throws IllegalArgumentException When the given file does not exist */
	@Throws(IllegalArgumentException::class)
	suspend fun deleteFile(filename: String)

	/** Gets whether or not the dialog for updating databases was shown today */
	fun getDbUpdateDialogLastShownDate(): Flow<Result<LocalDate>>
	suspend fun setUpdateDbDialogLastShownDate(date: LocalDate)

	/** Gets the version of the saved local input agency database */
	suspend fun getDatabaseVersion(databaseAgency: DatabaseAgency): Int
	suspend fun updateDatabaseVersion(databaseAgency: DatabaseAgency, version: Int)

	/** A flow of a list of all possible databases that the app can download, alongside their state */
	val databases: Flow<List<DatabaseState>>

	val databaseWorkNameState: Flow<Map<DatabaseAgency, Boolean>>
	suspend fun setRestartNeededFlag(databaseAgency: DatabaseAgency)
	suspend fun resetRestartNeededFlag()

	/** Checks if it is the first time that the app has been launched. **/
	suspend fun getIsFirstTime(): Boolean

	/** Initialises the isFirstTime flag to be set to false. */
	suspend fun setIsNotFirstTime()

	suspend fun setSelfSignedCert(cert: X509Certificate)

	suspend fun deleteDatabase(databaseAgency: DatabaseAgency)
}