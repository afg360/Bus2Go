package dev.mainhq.bus2go.preferences

import androidx.datastore.core.Serializer
import dev.mainhq.bus2go.MainActivity
import dev.mainhq.bus2go.utils.Time
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
data class AlarmsData (
    @Serializable(with = InnerPersistentListSerializer::class)
    val list : PersistentList<Alarm> = persistentListOf()
)

@Serializable
data class Alarm(
    //TODO add a unique id attribute so that alarms may have the same title
    val id : Int,
    /** Name of the alarm given. Should be unique */
    val title : String,
    /** Suppose to be the same as buses in the favourites */
    val busInfo : BusInfo,
    /** Time set for the alarm to ring before the  */
    val timeBefore : SerializableTime,
    /** Days when the alarm will have to ring */
    val ringDays : Map<Char, Boolean>,
    /** Stores if it's on or off */
    val isOn : Boolean
)

@Serializable
data class SerializableTime(
    val hour : Int,
    val min : Int,
    val sec : Int
)

@Suppress("EXTERNAL_SERIALIZER_USELESS")
@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class InnerPersistentListSerializer(private val serializer: KSerializer<Alarm>) :
    KSerializer<PersistentList<Alarm>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<AlarmsData>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<Alarm>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<Alarm> {
        return ListSerializer(serializer).deserialize(decoder).toPersistentList()
    }

}


object AlarmsSerializer : Serializer<AlarmsData> {
    override val defaultValue: AlarmsData
        get() = AlarmsData()

    override suspend fun readFrom(input: InputStream): AlarmsData {
        return try{
            Json.decodeFromString(AlarmsData.serializer(), input.readBytes().decodeToString())
        }
        catch (e : Exception){
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: AlarmsData, output: OutputStream) {
        output.write(
            Json.encodeToString(AlarmsData.serializer(), t).encodeToByteArray()
        )
    }
}