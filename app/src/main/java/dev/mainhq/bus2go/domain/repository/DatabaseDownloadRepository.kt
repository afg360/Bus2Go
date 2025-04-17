package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.DbToDownload

interface DatabaseDownloadRepository {

	suspend fun download(dbToDownload: DbToDownload): Boolean
}