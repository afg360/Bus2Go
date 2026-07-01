package dev.mainhq.bus2go.data.repository

import android.content.Context
import java.io.File

import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.AppVersions
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.Progress
import dev.mainhq.bus2go.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class DatabaseDownloadRepositoryImpl(
	applicationContext: Context,
	private val settingsRepository: SettingsRepository,
	override val networkMonitor: NetworkMonitor,
): DatabaseDownloadRepositoryAbstractImpl() {

	//we must make this call every time instead of holding a reference to the server choice because
	// it may change overtime
	override val baseHost
		get() = settingsRepository.getSettings().serverChoice

	override val tag = ""
	override val filesDir: File = applicationContext.filesDir
	override val logger = null

	private companion object {
		private const val DEFAULT_PORT = 8000 //443
	}

	override suspend fun getIsBus2Go(str: String): Result<Boolean> {
		return super._getIsBus2Go(str, DEFAULT_PORT)
	}

	override suspend fun getDbUpToDateVersion(dbToDownload: DbToDownload): Result<Int> {
		return super._getDbUpToDateVersion(dbToDownload, DEFAULT_PORT)
	}

	override suspend fun getAllDbUpToDateVersion(): Result<Map<DbToDownload, Int>> {
		return super._getAllDbUpToDateVersion(DEFAULT_PORT)
	}

	override suspend fun getAppVersionCodeRequired(): Result<AppVersions> {
		return super._getAppVersionCodeRequired(DEFAULT_PORT)
	}

	override fun getDb(dbToDownload: DbToDownload, versionNeeded: Int): Flow<Progress> {
		return super._getDb(dbToDownload, versionNeeded, DEFAULT_PORT)
	}

	override fun decompressFile(dbPath: String, dbName: String, version: Int): Flow<Progress> {
		return super._decompressFile(dbPath, dbName, version)
	}

}
