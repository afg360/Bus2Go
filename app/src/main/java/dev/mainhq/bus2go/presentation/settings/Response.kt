package dev.mainhq.bus2go.presentation.settings

data class Response(
	val isValid: Boolean,
	val string: String,
	val data: String?
)