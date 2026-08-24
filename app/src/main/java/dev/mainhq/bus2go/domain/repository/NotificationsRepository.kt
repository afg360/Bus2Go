package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.NotificationType

interface NotificationsRepository {

	fun notifyAppUpdates(notificationType: NotificationType.AppOperation)

	fun notifyDbUpdates(notificationType: NotificationType.DbOperation, databaseAgency: DatabaseAgency)
}