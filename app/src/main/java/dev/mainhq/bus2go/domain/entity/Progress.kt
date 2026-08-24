package dev.mainhq.bus2go.domain.entity

sealed class Progress {
	object Idle : Progress()
	data class Downloading(val current: Int, val contentLength: Int) : Progress()
	data class Completed(val success: Boolean) : Progress()
	data class Failed(val msg: String, val error: Exception) : Progress()
}
