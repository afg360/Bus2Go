package dev.mainhq.bus2go.presentation.stop_direction

sealed class ActivityFragment {
	data object Direction: ActivityFragment()
	data class Stops(
		val animationDirection: AnimationDirection,
	) : ActivityFragment()
}

enum class AnimationDirection {
	TO_TOP, TO_BOTTOM
}