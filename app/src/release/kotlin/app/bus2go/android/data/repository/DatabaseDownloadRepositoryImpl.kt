package app.bus2go.android.data.repository

import android.content.Context
import java.io.File

import app.bus2go.android.domain.core.Result
import app.bus2go.android.data.data_source.remote.NetworkMonitor
import app.bus2go.android.domain.core.Logger
import app.bus2go.android.domain.entity.AppVersions
import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.entity.Progress
import app.bus2go.android.domain.entity.ServerChoice
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.Flow

class DatabaseDownloadRepositoryImpl(
	applicationContext: Context,
	override val logger: Logger?,
	override val networkMonitor: NetworkMonitor
): DatabaseDownloadRepositoryAbstractImpl() {

	override val protocol = URLProtocol.HTTPS

	override val defaultPort = 443
	override val tag = ""
	override val filesDir: File = applicationContext.filesDir


	//TODO each function shall check whether it should be self hosted or not and use the correct port?

	override suspend fun getIsBus2Go(str: String, isSelfHosted: Boolean): Result<Boolean> {
		return super._getIsBus2Go(str, isSelfHosted)
	}

	override suspend fun getDbUpToDateVersion(serverChoice: ServerChoice, databaseAgency: DatabaseAgency): Result<Int> {
		return super._getDbUpToDateVersion(serverChoice, databaseAgency)
	}

	override suspend fun getAllDbUpToDateVersion(serverChoice: ServerChoice): Result<Map<DatabaseAgency, Int>> {
		return super._getAllDbUpToDateVersion(serverChoice)
	}

	override suspend fun getAppVersionCodeRequired(serverChoice: ServerChoice): Result<AppVersions> {
		return super._getAppVersionCodeRequired(serverChoice)
	}

	override fun downloadDb(serverChoice: ServerChoice, databaseAgency: DatabaseAgency, versionNeeded: Int): Flow<Progress> {
		return super._getDb(serverChoice, databaseAgency, versionNeeded)
	}

}
