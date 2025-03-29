package dev.mainhq.bus2go.domain.exceptions

class DirectionsMissingException(
	message: String? = null,
	cause: Throwable? = null
): Bus2GoBaseException(message, cause)