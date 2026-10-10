package app.bus2go.android.domain.exceptions


class DatabaseFormatingException(
	message: String? = null,
	cause: Throwable? = null
) : Bus2GoBaseException(message, cause)
