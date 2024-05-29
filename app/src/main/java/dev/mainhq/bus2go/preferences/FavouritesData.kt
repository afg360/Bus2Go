package dev.mainhq.bus2go.preferences

import android.os.Parcel
import android.os.Parcelable
import androidx.datastore.core.Serializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
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
import java.lang.Exception

//TODO eventually encrypt all the data to make it safe from other apps in case unwanted access happens

@Serializable
data class FavouritesData(
    @Serializable(with = MyPersistentListSerializer::class)
    val list : PersistentList<BusInfo> = persistentListOf()
)

@Serializable
//FIXME could improve the data stored inside for better ease of use
data class BusInfo(val stopName : String, val tripHeadsign : String)
    : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(stopName)
        dest.writeString(tripHeadsign)
    }

    companion object CREATOR : Parcelable.Creator<BusInfo> {
        override fun createFromParcel(parcel: Parcel): BusInfo {
            return BusInfo(parcel)
        }

        override fun newArray(size: Int): Array<BusInfo?> {
            return arrayOfNulls(size)
        }
    }
}

@Suppress("EXTERNAL_SERIALIZER_USELESS")
@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class MyPersistentListSerializer(private val serializer: KSerializer<BusInfo>) : KSerializer<PersistentList<BusInfo>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<BusInfo>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<BusInfo>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<BusInfo> {
        return ListSerializer(serializer).deserialize(decoder).toPersistentList()
    }

}

object SettingsSerializer : Serializer<FavouritesData> {
    override val defaultValue: FavouritesData
        get() = FavouritesData()

    override suspend fun readFrom(input: InputStream): FavouritesData {
        return try{
            Json.decodeFromString(FavouritesData.serializer(), input.readBytes().decodeToString())
        }
        catch (e : Exception){
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: FavouritesData, output: OutputStream) {
        output.write(
            Json.encodeToString(FavouritesData.serializer(), t).encodeToByteArray()
        )
    }
}

