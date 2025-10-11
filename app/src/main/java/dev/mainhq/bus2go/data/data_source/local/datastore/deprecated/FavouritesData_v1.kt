package dev.mainhq.bus2go.data.data_source.local.datastore.deprecated

import android.annotation.SuppressLint
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

@SuppressLint("UnsafeOptInUsageError")
@Deprecated(
	message = "Version 1 of the data. Lacked some important data.",
	replaceWith = ReplaceWith("FavouritesData")
)
@Serializable
data class FavouritesDataOld(
	@Serializable(with = PersistentStmBusInfoListSerializer::class)
	val listSTM : PersistentList<StmBusData> = persistentListOf(),
	@Serializable(with = PersistentExoBusInfoOldListSerializer::class)
	val listExo : PersistentList<ExoBusDataOld> = persistentListOf(),
	@Serializable(with = PersistentTrainInfoListSerializer_v1::class)
	val listExoTrain : PersistentList<ExoTrainData> = persistentListOf()
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@Parcelize
@Deprecated(
	message = "Migrated from version 1 to version 2",
	replaceWith = ReplaceWith("FavouritesData.kt #ExoBusData")
)
data class ExoBusDataOld(override val stopName : String,/** aka tripheadSign */ override val routeId : String, override val direction: String)
	: TransitDataDto_v2()

class PersistentExoBusInfoOldListSerializer(private val serializer: KSerializer<ExoBusDataOld>) :
	KSerializer<PersistentList<ExoBusDataOld>> {

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

@Deprecated(
	message = "Version 1 serializer, lacked some important data",
	replaceWith = ReplaceWith("SettingsSerializer")
)
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
