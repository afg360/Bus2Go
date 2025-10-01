package dev.mainhq.bus2go.domain.entity

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class AppVersions(
	val min: Int,
	val max: Int
)
