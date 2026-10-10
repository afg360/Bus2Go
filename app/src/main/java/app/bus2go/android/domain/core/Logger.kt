package app.bus2go.android.domain.core

interface Logger {
	fun debug(tag: String, message: String)
	fun info(tag: String, message: String)
	fun warn(tag: String, message: String)
	fun error(tag: String, message: String, throwable: Throwable? = null)
}