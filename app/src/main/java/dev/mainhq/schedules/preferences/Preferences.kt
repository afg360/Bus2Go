package dev.mainhq.schedules.preferences

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

@Serializable
data class Favourites(
    @Serializable(with = MyPersistentListSerializer::class)
    val list : PersistentList<BusInfo> = persistentListOf()
)

@Serializable
data class BusInfo(
    val busLine : String,
    val tripHeadsign : String,
)

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


object SettingsSerializer : Serializer<Favourites> {
    override val defaultValue: Favourites
        get() = Favourites()

    override suspend fun readFrom(input: InputStream): Favourites {
        return try{
            Json.decodeFromString(Favourites.serializer(), input.readBytes().decodeToString())
        }
        catch (e : Exception){
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: Favourites, output: OutputStream) {
        output.write(
            Json.encodeToString(Favourites.serializer(), t).encodeToByteArray()
        )
    }
}

