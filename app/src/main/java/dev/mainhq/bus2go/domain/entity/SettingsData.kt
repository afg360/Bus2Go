package dev.mainhq.bus2go.domain.entity

data class SettingsData(
	//FIXME instead of using strings use enums or some other classes (and ordinals?)
	val language: Int,
	val isDarkMode: Boolean,
	val serverChoice: ServerChoice,
	val isRealTimeOn: Boolean,
)
//TODO some other data class for settings related to data and synchronising
