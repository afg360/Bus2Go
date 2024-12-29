package dev.mainhq.bus2go.preferences

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.datastore.core.Serializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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
data class FavouritesData(
    @Serializable(with = PersistentStmBusInfoListSerializer::class)
    val listSTM : PersistentList<StmBusData> = persistentListOf(),
    @Serializable(with = PersistentExoBusInfoListSerializer::class)
    val listExo : PersistentList<ExoBusData> = persistentListOf(),
    @Serializable(with = PersistentTrainInfoListSerializer::class)
    val listExoTrain : PersistentList<TrainData> = persistentListOf()
)

@Serializable
data class FavouritesDataOld(
    @Serializable(with = PersistentStmBusInfoListSerializer::class)
    val listSTM : PersistentList<StmBusData> = persistentListOf(),
    @Serializable(with = PersistentExoBusInfoOldListSerializer::class)
    val listExo : PersistentList<ExoBusDataOld> = persistentListOf(),
    @Serializable(with = PersistentTrainInfoListSerializer::class)
    val listExoTrain : PersistentList<TrainData> = persistentListOf()
)

abstract class TransitData{
    abstract val routeId : String
    abstract val stopName : String
    abstract val direction : String
}

@Serializable
data class TrainData(override val stopName : String, override val routeId : String, val trainNum : Int, val routeName : String,
                     val directionId: Int, override val direction : String)
    : Parcelable, TransitData() {
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
data class StmBusData(override val stopName: String,/** aka busNum */override val routeId : String,
                      val directionId: Int, override val direction : String, val lastStop : String)
    : Parcelable, TransitData() {
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
@Deprecated("Migrated from this old version to the new below, called ExoBusData")
data class ExoBusDataOld(override val stopName : String,/** aka tripheadSign */ override val routeId : String, override val direction: String)
    : Parcelable, TransitData() {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(stopName)
        dest.writeString(routeId)
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
@Deprecated("This class is only used for migration purposes")
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class PersistentExoBusInfoOldListSerializer(private val serializer: KSerializer<ExoBusDataOld>) : KSerializer<PersistentList<ExoBusDataOld>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<ExoBusDataOld>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<ExoBusDataOld>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<ExoBusDataOld> {
        /** The decoder object retains its state. Even if we catch the exception, its state would have changed...
         *  This is why i am using this class in the first place, when trying to read the original inputStream */
        return ListSerializer(ExoBusDataOld.serializer()).deserialize(decoder).toPersistentList()
    }
}

@Serializable
data class ExoBusData(override val stopName : String, override val routeId : String,
                      override val direction: String, val routeLongName: String, val headsign: String)
    : Parcelable, TransitData() {
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

object SettingsSerializerOld : Serializer<FavouritesDataOld> {
    override val defaultValue: FavouritesDataOld
        get() = FavouritesDataOld()

    override suspend fun readFrom(input: InputStream): FavouritesDataOld {
        return try{
            /** First try to read the input stream as an old data. if it fails, retry. if that fails,
             *  then use the default data */
            Json.decodeFromString(FavouritesDataOld.serializer(), input.readBytes().decodeToString())
        }
        catch (e : Exception){
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: FavouritesDataOld, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(FavouritesDataOld.serializer(), t).encodeToByteArray()
            )
        }
    }
}
