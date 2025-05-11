package dev.mainhq.bus2go.presentation.stop_times

import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.presentation.main.home.favourites.Urgency

data class StopTimesDisplayModel(
	val arrivalTime: Time,
	val timeLeftTextDisplay: String,
	val urgency: Urgency
)
