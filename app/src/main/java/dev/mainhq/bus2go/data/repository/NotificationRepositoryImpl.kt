package dev.mainhq.bus2go.data.repository

import dev.mainhq.bus2go.data.data_source.notifications.NotificationHandler
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.repository.NotificationsRepository

class NotificationRepositoryImpl(
	private val notificationHandler: NotificationHandler
): NotificationsRepository {

	override fun notify(notificationType: NotificationType) {
		when(notificationType){
			is NotificationType.AppUpdateAvailable ->
				notificationHandler.notifyDbUpdateAvailable(notificationType.version)

			is NotificationType.AppUpdating ->
				notificationHandler.notifyAppUpdating(notificationType.percentage)

			NotificationType.AppUpdateDone -> notificationHandler.notifyAppUpdateDone()

			NotificationType.AppUpdateError -> notificationHandler.notifyAppUpdateFailed()

			is NotificationType.DbUpdateAvailable ->
				notificationHandler.notifyDbUpdateAvailable(notificationType.database)

			is NotificationType.DbDownloading ->
				notificationHandler.notifyDbDownloading(notificationType.progress)

			is NotificationType.DbExtracting -> notificationHandler.notifyDbExtracting()

			NotificationType.DbUpdateDone -> notificationHandler.notifyDbUpdateDone()

			NotificationType.DbUpdateError -> notificationHandler.notifyDbDownloadFailed()
		}
	}

}