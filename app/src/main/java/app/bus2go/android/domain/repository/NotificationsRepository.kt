package app.bus2go.android.domain.repository

import app.bus2go.android.domain.entity.DatabaseAgency
import app.bus2go.android.domain.entity.NotificationType

interface NotificationsRepository {

	fun notifyAppUpdates(notificationType: NotificationType.AppOperation)

	fun notifyDbUpdates(notificationType: NotificationType.DbOperation, databaseAgency: DatabaseAgency)
}