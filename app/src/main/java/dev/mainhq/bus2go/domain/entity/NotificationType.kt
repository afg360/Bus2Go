package dev.mainhq.bus2go.domain.entity

sealed class NotificationType {
	data class AppUpdateAvailable(val version: String): NotificationType()
	data class AppUpdating(val percentage: Int): NotificationType()
	data object AppUpdateDone: NotificationType()
	data object AppUpdateError: NotificationType()
	data class DbUpdateAvailable(val database: String): NotificationType()
	data class DbDownloading(val progress: Int): NotificationType()
	data object DbExtracting: NotificationType()
	data object DbUpdateDone: NotificationType()
	data object DbUpdateError: NotificationType()
}