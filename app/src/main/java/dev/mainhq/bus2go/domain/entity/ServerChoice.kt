package dev.mainhq.bus2go.domain.entity

/**
 * @param server The URL or domain name
 * @param isSelfHosted Whether `server` is self-hosted or not
 */
data class ServerChoice(
	val server: String,
	val isSelfHosted: Boolean
)
