package dev.mainhq.bus2go.domain.entity

import dev.mainhq.bus2go.utils.isExpired
import java.time.LocalDate

sealed class DatabaseState(
	open val db: DatabaseAgency,
) {
	sealed interface NotDownloaded

	data class DatabaseNotDownloaded(override val db: DatabaseAgency): DatabaseState(db), NotDownloaded

	data class DatabaseDownloading(
		override val db: DatabaseAgency,
		val currentProgress: NotificationType.DbOperation
	): DatabaseState(db)

	data class DatabaseDownloadError(
		override val db: DatabaseAgency
	): DatabaseState(db), NotDownloaded

	data class DatabaseDownloaded(
		override val db: DatabaseAgency,
		val sqliteVersion: Int,
		val expirationDate: LocalDate,
		val fileSize: Long
	): DatabaseState(db) {
		fun isExpired() = expirationDate.isExpired()
	}

	/** Used for when the database was downloaded, but the app needs to be reset */
	data class NeedAppRestart(
		override val db: DatabaseAgency
	): DatabaseState(db)
}