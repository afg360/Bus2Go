package dev.mainhq.bus2go.presentation.core


/** Very useful to not preinitialise hot flows with null values */
sealed class UiState<out T> {
	data class Success<T>(val data: T) : UiState<T>()
	data object Loading : UiState<Nothing>()
	data object Init : UiState<Nothing>()
	data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
}
