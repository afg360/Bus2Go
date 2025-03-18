package dev.mainhq.bus2go.data.data_source.local.preference.exo.entity

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
data class ExoFavouritesDataDto(
	val version: Int,
	@Serializable(with = PersistentExoBusInfoListSerializer::class)
	val listExo : PersistentList<ExoFavouriteBusItemDto> = persistentListOf(),
	@Serializable(with = PersistentTrainInfoListSerializer::class)
	val listExoTrain : PersistentList<ExoFavouriteTrainItemDto> = persistentListOf()
)

object ExoFavouritesDataSerializer : Serializer<ExoFavouritesDataDto> {
	override val defaultValue: ExoFavouritesDataDto
		get() = ExoFavouritesDataDto(1)

	override suspend fun readFrom(input: InputStream): ExoFavouritesDataDto {
		return try{
			/** First try to read the input stream as an old data. if it fails, retry. if that fails,
			 *  then use the default data */
			Json.decodeFromString(ExoFavouritesDataDto.serializer(), input.readBytes().decodeToString())
		}
		catch (e : Exception){
			e.printStackTrace()
			defaultValue
		}
	}

	override suspend fun writeTo(t: ExoFavouritesDataDto, output: OutputStream) {
		withContext(Dispatchers.IO) {
			output.write(
				Json.encodeToString(ExoFavouritesDataDto.serializer(), t).encodeToByteArray()
			)
		}
	}
}

