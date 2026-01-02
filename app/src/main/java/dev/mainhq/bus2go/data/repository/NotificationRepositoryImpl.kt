package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.notifications.NotificationHandler
import dev.mainhq.bus2go.domain.entity.DbToDownload
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.repository.NotificationsRepository

class NotificationRepositoryImpl(
	private val notificationHandler: NotificationHandler
): NotificationsRepository {

	@Throws(IllegalArgumentException::class)
	/** @throws IllegalArgumentException When giving a DbToDownload.ALL notif type */
	override fun notify(notificationType: NotificationType) {
		when(notificationType){
			is NotificationType.AppUpdateAvailable ->
				notificationHandler.notifyAppUpdateAvailable(notificationType.version)

			is NotificationType.AppUpdating ->
				notificationHandler.notifyAppUpdating(
					notificationType.current,
					notificationType.contentLength
				)

			NotificationType.AppUpdateDone -> notificationHandler.notifyAppUpdateDone()

			NotificationType.AppUpdateError -> notificationHandler.notifyAppUpdateFailed()

			is NotificationType.DbUpdateAvailable ->
				notificationHandler.notifyDbUpdateAvailable(notificationType.database)

			is NotificationType.DbDownloading ->
				notificationHandler.notifyDbDownloading(
					notificationType.database,
					notificationType.current,
					notificationType.contentLength
				)

			is NotificationType.DbExtracting -> notificationHandler.notifyDbExtracting(notificationType.database)

			is NotificationType.DbUpdateDone -> notificationHandler.notifyDbUpdateDone(notificationType.database)

			is NotificationType.DbUpdateError -> notificationHandler.notifyDbDownloadFailed(notificationType.database)
		}
	}

}