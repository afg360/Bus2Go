package dev.mainhq.bus2go.data.data_source.local.preference.deprecated

import android.os.Parcel
import android.os.Parcelable
import androidx.datastore.core.Serializer
import dev.mainhq.bus2go.data.data_source.local.preference.TransitDataDto
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
@Deprecated(
    message = "This version mixes up STM data and Exo data, which is not ideal for app architecture",
    replaceWith = ReplaceWith("ExoFavouritesData, StmFavouritesData")
)
data class FavouritesData(
    @Serializable(with = PersistentStmBusInfoListSerializer::class)
    val listSTM : PersistentList<StmBusData> = persistentListOf(),
    @Serializable(with = PersistentExoBusInfoListSerializer::class)
    val listExo : PersistentList<ExoBusData> = persistentListOf(),
    @Serializable(with = PersistentTrainInfoListSerializer::class)
    val listExoTrain : PersistentList<ExoTrainData> = persistentListOf()
)

@Serializable
data class ExoTrainData(override val stopName : String, override val routeId : String, val trainNum : Int, val routeName : String,
                        val directionId: Int, override val direction : String)
    : TransitDataDto() {
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

    companion object CREATOR : Parcelable.Creator<ExoTrainData> {
        override fun createFromParcel(parcel: Parcel): ExoTrainData {
            return ExoTrainData(parcel)
        }

        override fun newArray(size: Int): Array<ExoTrainData?> {
            return arrayOfNulls(size)
        }
    }
}

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

@Serializable
data class StmBusData(override val stopName: String,/** aka busNum */override val routeId : String,
                      val directionId: Int, override val direction : String, val lastStop : String)
    : TransitDataDto() {
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

    companion object CREATOR : Parcelable.Creator<StmBusData> {
        override fun createFromParcel(parcel: Parcel): StmBusData {
            return StmBusData(parcel)
        }

        override fun newArray(size: Int): Array<StmBusData?> {
            return arrayOfNulls(size)
        }
    }
}

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


@Serializable
data class ExoBusData(override val stopName : String, override val routeId : String,
                      override val direction: String, val routeLongName: String, val headsign: String)
    : Parcelable, TransitDataDto() {
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

    companion object CREATOR : Parcelable.Creator<ExoBusData> {
        override fun createFromParcel(parcel: Parcel): ExoBusData {
            return ExoBusData(parcel)
        }

        override fun newArray(size: Int): Array<ExoBusData?> {
            return arrayOfNulls(size)
        }
    }
}

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

