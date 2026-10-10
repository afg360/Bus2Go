package app.bus2go.android.data.core

import android.util.Log
import app.bus2go.android.domain.core.Logger

class LoggerImpl: Logger {

	override fun debug(tag: String, message: String) {
		Log.d(tag, message)
	}

	override fun info(tag: String, message: String) {
		Log.i(tag, message)
	}

	override fun warn(tag: String, message: String) {
		Log.w(tag, message)
	}

	override fun error(tag: String, message: String, throwable: Throwable?) {
		if (throwable != null)
			Log.e(tag, message, throwable)
		else Log.e(tag, message)
	}

}