package dev.mainhq.bus2go.data.data_source.local.preference.stm.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.datastore.core.Serializer
import dev.mainhq.bus2go.domain.entity.TransitData
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream


//TODO eventually encrypt all the data to make it safe from other apps in case unwanted access happens
@Serializable
data class StmFavouritesData(
	@Serializable(with = PersistentStmBusInfoListSerializer::class)
	val listSTM : PersistentList<StmFavouriteBusItem> = persistentListOf(),
)

@Serializable
/**
 * @param routeId aka busNum.
 **/
data class StmFavouriteBusItem(
	override val stopName: String,
	override val routeId : String,
	val directionId: Int,
	override val direction : String,
	val lastStop : String
) : TransitData() {

	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readInt(),
		parcel.readString()!!,
		parcel.readString()!!
	)

	override fun describeContents(): Int {
		return 0
	}

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(stopName)
		dest.writeString(routeId)
		dest.writeInt(directionId)
		dest.writeString(direction)
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

object StmFavouritesDataSerializer : Serializer<StmFavouritesData> {
	override val defaultValue: StmFavouritesData
		get() = StmFavouritesData()

	override suspend fun readFrom(input: InputStream): StmFavouritesData {
		return try{
			/** First try to read the input stream as an old data. if it fails, retry. if that fails,
			 *  then use the default data */
			Json.decodeFromString(StmFavouritesData.serializer(), input.readBytes().decodeToString())
		}
		catch (e : Exception){
			e.printStackTrace()
			defaultValue
		}
	}

	override suspend fun writeTo(t: StmFavouritesData, output: OutputStream) {
		withContext(Dispatchers.IO) {
			output.write(
				Json.encodeToString(StmFavouritesData.serializer(), t).encodeToByteArray()
			)
		}
	}
}

