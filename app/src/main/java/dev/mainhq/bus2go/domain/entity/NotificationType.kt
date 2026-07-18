package dev.mainhq.bus2go.domain.entity

sealed class NotificationType {
	data class AppUpdateAvailable(val version: String): NotificationType()
	data class AppUpdating(val current: Int, val contentLength: Int): NotificationType()
	data object AppUpdateDone: NotificationType()
	data object AppUpdateError: NotificationType()
	data class DbUpdateAvailable(val database: DatabaseAgency): NotificationType()
	data class DbDownloading(val database: DatabaseAgency, val current: Int, val contentLength: Int): NotificationType()
	data class DbExtracting(val database: DatabaseAgency): NotificationType()
	data class DbUpdateDone(val database: DatabaseAgency): NotificationType()
	data class DbUpdateError(val database: DatabaseAgency): NotificationType()
}