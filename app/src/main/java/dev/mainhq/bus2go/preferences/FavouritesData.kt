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
    @Serializable(with = PersistentExoBusInfoListSerializer::class)
    val listSTM : PersistentList<StmBusData> = persistentListOf(),
    @Serializable(with = PersistentExoBusInfoListSerializer::class)
    val listExo : PersistentList<ExoBusData> = persistentListOf(),
    //TODO MAY BE REMOVED
    @Serializable(with = PersistentTrainInfoListSerializer::class)
    val listExoTrain : PersistentList<TrainData> = persistentListOf()
)

interface TransitData

@Serializable
data class TrainData(val stopName : String, val routeId : Int, val trainNum : Int, val routeName : String,
                     val directionId: Int, val direction : String)
    : Parcelable, TransitData {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
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
        dest.writeInt(routeId)
        dest.writeInt(trainNum)
        dest.writeString(routeName)
        dest.writeInt(directionId)
        dest.writeString(direction)
    }

    companion object CREATOR : Parcelable.Creator<TrainData> {
        override fun createFromParcel(parcel: Parcel): TrainData {
            return TrainData(parcel)
        }

        override fun newArray(size: Int): Array<TrainData?> {
            return arrayOfNulls(size)
        }
    }
}

@Suppress("EXTERNAL_SERIALIZER_USELESS")
@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class PersistentTrainInfoListSerializer(private val serializer: KSerializer<TrainData>) : KSerializer<PersistentList<TrainData>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<TrainData>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<TrainData>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<TrainData> {
        return ListSerializer(serializer).deserialize(decoder).toPersistentList()
    }
}

@Serializable
data class StmBusData(val stopName: String, val busNum : Int, val directionId: Int, val direction : String)
    : Parcelable, TransitData {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(stopName)
        dest.writeInt(busNum)
        dest.writeInt(directionId)
        dest.writeString(direction)
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

@Suppress("EXTERNAL_SERIALIZER_USELESS")
@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
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
//FIXME could improve the data stored inside for better ease of use
data class ExoBusData(val stopName : String, val tripHeadsign : String, val direction: String)
    : Parcelable, TransitData {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(stopName)
        dest.writeString(tripHeadsign)
        dest.writeString(direction)
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

@Suppress("EXTERNAL_SERIALIZER_USELESS")
@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
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