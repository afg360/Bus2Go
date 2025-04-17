package dev.mainhq.bus2go.domain.core


sealed class Result<out T> {
	data class Success<T>(val data: T): Result<T>()
	data class Error(val throwable: Throwable?): Result<Nothing>()

}