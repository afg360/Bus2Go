package app.bus2go.android.data.repository

import app.bus2go.android.data.data_source.notifications.NotificationHandler
import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.entity.NotificationType
import app.bus2go.android.domain.repository.NotificationsRepository

class NotificationRepositoryImpl(
	private val notificationHandler: NotificationHandler
): NotificationsRepository {

	override fun notifyAppUpdates(notificationType: NotificationType.AppOperation) {
		when(notificationType) {
			is NotificationType.AppUpdateAvailable ->
				notificationHandler.notifyAppUpdateAvailable(notificationType.version)

			is NotificationType.AppUpdating ->
				notificationHandler.notifyAppUpdating(
					notificationType.current,
					notificationType.contentLength
				)

			NotificationType.AppUpdateDone -> notificationHandler.notifyAppUpdateDone()

			NotificationType.AppUpdateError -> notificationHandler.notifyAppUpdateFailed()
		}
	}

	override fun notifyDbUpdates(notificationType: NotificationType.DbOperation, databaseAgency: DatabaseAgency) {
		when(notificationType){
			is NotificationType.DbUpdateAvailable ->
				notificationHandler.notifyDbUpdateAvailable(databaseAgency)

			is NotificationType.DbEnqueued -> notificationHandler.notifyDbDownloadStarted(databaseAgency)

			is NotificationType.DbDownloading ->
				notificationHandler.notifyDbDownloading(
					databaseAgency,
					notificationType.current,
					notificationType.contentLength
				)

			is NotificationType.DbExtracting -> notificationHandler.notifyDbExtracting(databaseAgency)

			is NotificationType.DbUpdateDone -> notificationHandler.notifyDbUpdateDone(databaseAgency)

			NotificationType.DbUpdateCancelled -> notificationHandler.notifyDbDownloadCancelled(databaseAgency)

			is NotificationType.DbUpdateError -> notificationHandler.notifyDbDownloadFailed(notificationType.error, databaseAgency)
		}
	}
}