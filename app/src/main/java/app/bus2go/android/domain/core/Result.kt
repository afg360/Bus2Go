package app.bus2go.android.domain.core


sealed class Result<out T> {
	data class Success<T>(val data: T): Result<T>()
	data class Error(val throwable: Throwable?, val message: String? = null): Result<Nothing>()
}