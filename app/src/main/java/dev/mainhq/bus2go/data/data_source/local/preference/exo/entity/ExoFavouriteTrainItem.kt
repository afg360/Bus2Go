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
data class ExoFavouriteTrainItem(
	override val stopName : String,
	override val routeId : String,
	val trainNum : Int,
	val routeName : String,
	val directionId: Int,
	override val direction : String
) : Parcelable, TransitData() {
	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readInt(),
		parcel.readString()!!,
		parcel.readInt(),
		parcel.readString()!!
	)

	override fun describeContents(): Int {
		return 0
	}

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(stopName)
		dest.writeString(routeId)
		dest.writeInt(trainNum)
		dest.writeString(routeName)
		dest.writeInt(directionId)
		dest.writeString(direction)
	}

	companion object CREATOR : Parcelable.Creator<ExoFavouriteTrainItem> {
		override fun createFromParcel(parcel: Parcel): ExoFavouriteTrainItem {
			return ExoFavouriteTrainItem(parcel)
		}

		override fun newArray(size: Int): Array<ExoFavouriteTrainItem?> {
			return arrayOfNulls(size)
		}
	}
}

class PersistentTrainInfoListSerializer(private val serializer: KSerializer<ExoFavouriteTrainItem>) :
	KSerializer<PersistentList<ExoFavouriteTrainItem>> {

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<List<ExoFavouriteTrainItem>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()

	override fun serialize(encoder: Encoder, value: PersistentList<ExoFavouriteTrainItem>) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<ExoFavouriteTrainItem> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}
