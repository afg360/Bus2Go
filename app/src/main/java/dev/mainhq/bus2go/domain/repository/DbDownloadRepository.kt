package dev.mainhq.bus2go.domain.repository

interface DbDownloadRepository {

	suspend fun downloadAllDb()
	suspend fun downloadStmDb()
	suspend fun downloadExoDb()
}