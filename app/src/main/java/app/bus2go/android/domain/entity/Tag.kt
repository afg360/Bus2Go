package app.bus2go.android.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(
	val label: String,
	val color: Int
): Parcelable