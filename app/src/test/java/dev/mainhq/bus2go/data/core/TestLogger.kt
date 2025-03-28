package dev.mainhq.bus2go.data.core

import dev.mainhq.bus2go.domain.core.Logger

class TestLogger: Logger {
	private val logger: java.util.logging.Logger = java.util.logging.Logger.getLogger("TestLogger")

	override fun debug(tag: String, message: String) {
		println("$tag - $message")
	}

	override fun info(tag: String, message: String) {
		logger.info("$tag - $message")
	}

	override fun warn(tag: String, message: String) {
		logger.warning("$tag - $message")
	}

	override fun error(tag: String, message: String, throwable: Throwable?) {
		logger.severe("$tag - $message")
	}
}