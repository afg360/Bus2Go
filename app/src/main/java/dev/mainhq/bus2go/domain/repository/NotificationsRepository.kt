package dev.mainhq.bus2go.domain.repository

import dev.mainhq.bus2go.domain.entity.NotificationType

interface NotificationsRepository {

	fun notify(notificationType: NotificationType)
}