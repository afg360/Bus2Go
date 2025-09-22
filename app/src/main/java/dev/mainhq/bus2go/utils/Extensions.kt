package dev.mainhq.bus2go.utils

import com.google.android.material.datepicker.DateValidatorPointForward
import java.time.LocalDate


/** Assumes that the long in question is in milliseconds */
fun Long.toEpochDay(): Long{
	return this / (3600 * 1000 * 24)
}

fun LocalDate.toEpochMillis(): Long {
	return this.toEpochDay() * 24 * 3600 * 1000
}
