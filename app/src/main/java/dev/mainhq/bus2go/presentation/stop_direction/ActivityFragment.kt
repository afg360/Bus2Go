package dev.mainhq.bus2go.presentation.stop_direction


sealed class ActivityFragment {
	data object Direction: ActivityFragment()
	data class Stops(
		val animationDirection: AnimationDirection,
	) : ActivityFragment()
}

//FIXME instead of using those to always store animations, store them in the sealed class above as
// SharedFlow without any caching, so that the animation is always only triggered once
// or something similar
// (bug arises on configuration change)
// could also instead save the fragment screen on pause of the full activity
enum class AnimationDirection {
	TO_TOP, TO_BOTTOM, FROM_TOP, FROM_BOTTOM, FROM_TIMES_ACTIVITY
}