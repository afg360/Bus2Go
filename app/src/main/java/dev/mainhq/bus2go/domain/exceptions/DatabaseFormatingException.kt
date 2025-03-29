package dev.mainhq.bus2go.domain.exceptions


class DatabaseFormatingException(
	message: String? = null,
	cause: Throwable? = null
) : Bus2GoBaseException(message, cause)
