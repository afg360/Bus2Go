package app.bus2go.android.presentation.stop_times

import app.bus2go.android.domain.entity.Time
import app.bus2go.android.presentation.main.home.Urgency

data class StopTimesDisplayModel(
	val arrivalTime: Time,
	val timeLeftTextDisplay: String,
	val urgency: Urgency
)
