package app.bus2go.android.presentation.config

sealed class ConfigButtonState {
	data class Next(val text: String = "Continue", val onClick: () -> Unit): ConfigButtonState()
	data class Skip(val onClick: () -> Unit): ConfigButtonState()
	data object Loading: ConfigButtonState()
}
