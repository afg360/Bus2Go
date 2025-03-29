package dev.mainhq.bus2go.domain.exceptions

abstract class Bus2GoBaseException(
	message: String? = null,
	cause: Throwable? = null
) : Exception(message, cause)
