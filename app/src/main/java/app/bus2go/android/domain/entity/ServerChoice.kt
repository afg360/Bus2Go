package app.bus2go.android.domain.entity

/**
 * @param server The URL or domain name
 * @param isSelfHosted Whether `server` is self-hosted or not
 */
data class ServerChoice(
	val server: String,
	val isSelfHosted: Boolean
)
