package dev.mainhq.bus2go.domain.entity.exo

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

data class ExoFavouriteTrainItem(
	override val routeId : String,
	override val stopName : String,
	override val direction : String,
	val trainNum : Int,
	val routeName : String,
	val directionId: Int
) : FavouriteTransitData() {

	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readInt(),
		parcel.readString()!!,
		parcel.readInt()
	)


	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(routeId)
		dest.writeString(stopName)
		dest.writeString(direction)
		dest.writeInt(trainNum)
		dest.writeString(routeName)
		dest.writeInt(directionId)
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