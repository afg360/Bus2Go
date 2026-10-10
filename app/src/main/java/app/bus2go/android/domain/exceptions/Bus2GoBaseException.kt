package app.bus2go.android.domain.exceptions

abstract class Bus2GoBaseException(
	message: String? = null,
	cause: Throwable? = null
) : Exception(message, cause)
