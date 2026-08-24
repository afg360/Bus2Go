package dev.mainhq.bus2go.domain.entity


sealed class NotificationType {
	sealed interface AppOperation
	sealed interface DbOperation

	data class AppUpdateAvailable(val version: String): NotificationType(), AppOperation
	data class AppUpdating(val current: Int, val contentLength: Int): NotificationType(), AppOperation
	data object AppUpdateDone: NotificationType(), AppOperation
	data object AppUpdateError: NotificationType(), AppOperation

	data object DbUpdateAvailable: NotificationType(), DbOperation
	data object DbEnqueued: NotificationType(), DbOperation
	data class DbDownloading(val current: Int, val contentLength: Int): NotificationType(), DbOperation
	data object DbExtracting: NotificationType(), DbOperation
	data object DbUpdateDone: NotificationType(), DbOperation
	data class DbUpdateError(val error: String? = null): NotificationType(), DbOperation

	//used for when in idle states or nothing happens but still need to return something
	data object Nothing: NotificationType()
}