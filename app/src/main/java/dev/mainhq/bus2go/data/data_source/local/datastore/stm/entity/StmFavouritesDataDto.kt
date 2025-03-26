package dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity

import androidx.datastore.core.Serializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream


//TODO eventually encrypt all the data to make it safe from other apps in case unwanted access happens
@Serializable
data class StmFavouritesDataDto(
	val version: Int,
	@Serializable(with = PersistentStmBusInfoListSerializer::class)
	val listSTM : PersistentList<StmFavouriteBusItemDto> = persistentListOf()
)


object StmFavouritesDataSerializer : Serializer<StmFavouritesDataDto> {
	override val defaultValue: StmFavouritesDataDto
		get() = StmFavouritesDataDto(1)

	override suspend fun readFrom(input: InputStream): StmFavouritesDataDto {
		return try{
			/** First try to read the input stream as an old data. if it fails, retry. if that fails,
			 *  then use the default data */
			Json.decodeFromString(StmFavouritesDataDto.serializer(), input.readBytes().decodeToString())
		}
		catch (e : Exception){
			e.printStackTrace()
			defaultValue
		}
	}

	override suspend fun writeTo(t: StmFavouritesDataDto, output: OutputStream) {
		withContext(Dispatchers.IO) {
			output.write(
				Json.encodeToString(StmFavouritesDataDto.serializer(), t).encodeToByteArray()
			)
		}
	}
}

