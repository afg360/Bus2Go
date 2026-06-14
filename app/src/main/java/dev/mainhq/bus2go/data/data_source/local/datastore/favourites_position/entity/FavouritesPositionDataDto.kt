package dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.entity

import android.annotation.SuppressLint
import androidx.datastore.core.Serializer
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.PersistentStmBusInfoListSerializer
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouriteBusItemDto
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

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FavouritesPositionDataDto(
	//perhaps instead store the server version...?
	val version: Int,
	@Serializable(with = PersistentPositionListSerializer::class)
	val listFavouritesPosition : PersistentList<PositionDto> = persistentListOf()
)

class PersistentPositionListSerializer(private val serializer: KSerializer<PositionDto>)
	: KSerializer<PersistentList<PositionDto>> {

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<PersistentList<PositionDto>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()

	override fun serialize(encoder: Encoder, value: PersistentList<PositionDto>) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<PositionDto> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}

object FavouritesPositionDataSerializer : Serializer<FavouritesPositionDataDto> {
	override val defaultValue: FavouritesPositionDataDto
		get() = FavouritesPositionDataDto(1)

	override suspend fun readFrom(input: InputStream): FavouritesPositionDataDto {
		return try{
			/** First try to read the input stream as an old data. if it fails, retry. if that fails,
			 *  then use the default data */
			Json.decodeFromString(FavouritesPositionDataDto.serializer(), input.readBytes().decodeToString())
		}
		catch (e : Exception){
			e.printStackTrace()
			defaultValue
		}
	}

	override suspend fun writeTo(t: FavouritesPositionDataDto, output: OutputStream) {
		withContext(Dispatchers.IO) {
			output.write(
				Json.encodeToString(FavouritesPositionDataDto.serializer(), t).encodeToByteArray()
			)
		}
	}
}
