package dev.mainhq.bus2go.data.data_source.local.datastore.tags

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
@Parcelize
data class TagDto (
	val label: String,
	val color: Int
) : Comparable<TagDto>, Parcelable {
	override fun compareTo(other: TagDto): Int {
		return label.compareTo(other.label)
	}
}