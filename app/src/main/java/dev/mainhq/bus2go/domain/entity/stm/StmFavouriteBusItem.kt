package dev.mainhq.bus2go.domain.entity.stm

import android.os.Parcel
import android.os.Parcelable
import dev.mainhq.bus2go.data.data_source.local.preference.TransitDataDto
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
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
/**
 * @param routeId aka busNum.
 **/
data class StmFavouriteBusItem(
	override val routeId : String,
	override val stopName: String,
	override val direction : String,
	val directionId: Int,
	val lastStop : String
) : FavouriteTransitData() {

	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readInt(),
		parcel.readString()!!
	)

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(routeId)
		dest.writeString(stopName)
		dest.writeString(direction)
		dest.writeInt(directionId)
		dest.writeString(lastStop)
	}

	companion object CREATOR : Parcelable.Creator<StmFavouriteBusItem> {
		override fun createFromParcel(parcel: Parcel): StmFavouriteBusItem {
			return StmFavouriteBusItem(parcel)
		}

		override fun newArray(size: Int): Array<StmFavouriteBusItem?> {
			return arrayOfNulls(size)
		}
	}
}

class PersistentStmBusInfoListSerializer(private val serializer: KSerializer<StmFavouriteBusItem>) :
	KSerializer<PersistentList<StmFavouriteBusItem>> {

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<List<StmFavouriteBusItem>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()

	override fun serialize(encoder: Encoder, value: PersistentList<StmFavouriteBusItem>) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<StmFavouriteBusItem> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}

