package dev.mainhq.bus2go.data.data_source.local.preference.exo.entity

import android.os.Parcel
import android.os.Parcelable
import dev.mainhq.bus2go.domain.entity.TransitData
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ExoFavouriteBusItem(
	override val stopName : String,
	override val routeId : String,
	override val direction: String,
	val routeLongName: String,
	val headsign: String
) : Parcelable, TransitData() {
	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!
	)

	override fun describeContents(): Int {
		return 0
	}

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(stopName)
		dest.writeString(routeId)
		dest.writeString(direction)
		dest.writeString(routeLongName)
		dest.writeString(headsign)
	}

	companion object CREATOR : Parcelable.Creator<ExoFavouriteBusItem> {
		override fun createFromParcel(parcel: Parcel): ExoFavouriteBusItem {
			return ExoFavouriteBusItem(parcel)
		}

		override fun newArray(size: Int): Array<ExoFavouriteBusItem?> {
			return arrayOfNulls(size)
		}
	}
}

class PersistentExoBusInfoListSerializer(private val serializer: KSerializer<ExoFavouriteBusItem>) :
	KSerializer<PersistentList<ExoFavouriteBusItem>> {

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<List<ExoFavouriteBusItem>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()

	override fun serialize(encoder: Encoder, value: PersistentList<ExoFavouriteBusItem>) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<ExoFavouriteBusItem> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}
