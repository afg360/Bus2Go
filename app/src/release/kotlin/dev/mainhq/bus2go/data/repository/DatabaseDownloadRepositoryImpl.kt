package dev.mainhq.bus2go.data.repository

import android.content.Context
import java.io.File

import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.Progress
import dev.mainhq.bus2go.domain.entity.ServerChoice
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.Flow

class DatabaseDownloadRepositoryImpl(
	applicationContext: Context,
	override val networkMonitor: NetworkMonitor
): DatabaseDownloadRepositoryAbstractImpl() {

	override val protocol = URLProtocol.HTTPS

	override val defaultPort = 443
	override val tag = ""
	override val filesDir: File = applicationContext.filesDir
	override val logger = null


	//TODO each function shall check whether it should be self hosted or not and use the correct port?

	override suspend fun getIsBus2Go(str: String, isSelfHosted: Boolean): Result<Boolean> {
		return super._getIsBus2Go(str, isSelfHosted)
	}

	override suspend fun getDbUpToDateVersion(serverChoice: ServerChoice, dbToDownload: DbToDownload): Result<Int> {
		return super._getDbUpToDateVersion(serverChoice, dbToDownload)
	}

	override suspend fun getAllDbUpToDateVersion(serverChoice: ServerChoice): Result<Map<DbToDownload, Int>> {
		return super._getAllDbUpToDateVersion(serverChoice)
	}

	override suspend fun getAppVersionCodeRequired(serverChoice: ServerChoice): Result<AppVersions> {
		return super._getAppVersionCodeRequired(serverChoice)
	}

	override fun getDb(serverChoice: ServerChoice, dbToDownload: DbToDownload, versionNeeded: Int): Flow<Progress> {
		return super._getDb(serverChoice, dbToDownload, versionNeeded)
	}

}
