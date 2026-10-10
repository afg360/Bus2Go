package app.bus2go.android.presentation.main

sealed class UpdateDbState {
	object NoShow: UpdateDbState()
	object NotConnectedToInternet: UpdateDbState()
	data class Error(val text: String): UpdateDbState()
	data class Show(val text: String): UpdateDbState()
}