package dev.mainhq.bus2go.data.data_source.local.datastore.deprecated

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.datastore.core.Serializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
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
@SuppressLint("UnsafeOptInUsageError")
@Serializable
@Deprecated(
    message = "This version mixes up STM data and Exo data, which is not ideal for app architecture",
    replaceWith = ReplaceWith("ExoFavouritesData, StmFavouritesData")
)
data class FavouritesData(
    @Serializable(with = PersistentStmBusInfoListSerializer::class)
    val listSTM : PersistentList<StmBusData> = persistentListOf(),
    @Serializable(with = PersistentExoBusInfoListSerializer_v1::class)
    val listExo : PersistentList<ExoBusData> = persistentListOf(),
    @Serializable(with = PersistentTrainInfoListSerializer_v1::class)
    val listExoTrain : PersistentList<ExoTrainData> = persistentListOf()
)

@SuppressLint("UnsafeOptInUsageError")
@Parcelize
@Serializable
data class ExoTrainData(
    override val stopName: String,
    override val routeId: String,
    val trainNum: Int,
    val routeName: String,
    val directionId: Int,
    override val direction: String,
) : TransitDataDto_v2()

class PersistentTrainInfoListSerializer(private val serializer: KSerializer<ExoTrainData>) : KSerializer<PersistentList<ExoTrainData>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<ExoTrainData>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<ExoTrainData>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<ExoTrainData> {
        return ListSerializer(serializer).deserialize(decoder).toPersistentList()
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Parcelize
@Serializable
@Deprecated("Deprecated since V3", replaceWith = ReplaceWith("StmFavouriteBusItemDto"))
/** @param routeId aka busNum */
data class StmBusData(
    override val stopName: String,
    override val routeId: String,
    val directionId: Int,
    override val direction: String,
    val lastStop: String,
) : TransitDataDto_v2()

class PersistentStmBusInfoListSerializer(private val serializer: KSerializer<StmBusData>) : KSerializer<PersistentList<StmBusData>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<StmBusData>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<StmBusData>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<StmBusData> {
        return ListSerializer(serializer).deserialize(decoder).toPersistentList()
    }
}


@SuppressLint("UnsafeOptInUsageError")
@Parcelize
@Serializable
data class ExoBusData(
    override val stopName: String,
    override val routeId: String,
    override val direction: String,
    val routeLongName: String,
    val headsign: String,
) : Parcelable, TransitDataDto_v2()

class PersistentExoBusInfoListSerializer(private val serializer: KSerializer<ExoBusData>) : KSerializer<PersistentList<ExoBusData>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<ExoBusData>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<ExoBusData>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<ExoBusData> {
        return ListSerializer(serializer).deserialize(decoder).toPersistentList()
    }
}


@Deprecated(
    message = "Mixes data from STM and Exo, which was not ideal for the overall app architecture",
    replaceWith = ReplaceWith("ExoFavouritesDataSerializer, StmFavouritesDataSerializer")
)
object SettingsSerializer : Serializer<FavouritesData> {
    override val defaultValue: FavouritesData
        get() = FavouritesData()

    override suspend fun readFrom(input: InputStream): FavouritesData {
        return try{
            /** First try to read the input stream as an old data. if it fails, retry. if that fails,
             *  then use the default data */
            Json.decodeFromString(FavouritesData.serializer(), input.readBytes().decodeToString())
        }
        catch (e : Exception){
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: FavouritesData, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(FavouritesData.serializer(), t).encodeToByteArray()
            )
        }
    }
}

