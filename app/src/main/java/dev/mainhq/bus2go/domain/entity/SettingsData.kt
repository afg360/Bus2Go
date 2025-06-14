package dev.mainhq.bus2go.domain.entity

data class SettingsData(
	//FIXME instead of using strings use enums or some other classes
	val language: String,
	val isDarkMode: Boolean,
	val serverChoice: String,
	val isRealTime: Boolean,
)
//TODO some other data class for settings related to data and synchronising
