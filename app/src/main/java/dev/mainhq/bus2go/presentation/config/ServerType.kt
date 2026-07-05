package dev.mainhq.bus2go.presentation.config

enum class ServerType(private val str: String) {
	SELF_HOSTED("Self-Hosted"), WEB("Web");

	override fun toString() = str

	operator fun not(): ServerType {
		return if (this == SELF_HOSTED) {
			WEB
		}
		else {
			SELF_HOSTED
		}
	}
}