package dev.mainhq.bus2go.data.data_source.local.datastore.deprecated

import android.annotation.SuppressLint
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
@Deprecated("use v2")
@SuppressLint("UnsafeOptInUsageError")
data class ExoFavouritesDataDto_v1 (
	val version: Int,
	@Serializable(with = PersistentExoBusInfoListSerializer_v1::class)
	val listExo : PersistentList<ExoFavouriteBusItemDto_v1> = persistentListOf(),
	@Serializable(with = PersistentTrainInfoListSerializer_v1::class)
	val listExoTrain : PersistentList<ExoFavouriteTrainItemDto_v1> = persistentListOf()
)

@Deprecated("Use v2")
object ExoFavouritesDataSerializer_v1 : Serializer<ExoFavouritesDataDto_v1> {
	override val defaultValue: ExoFavouritesDataDto_v1
		get() = ExoFavouritesDataDto_v1(1)

	override suspend fun readFrom(input: InputStream): ExoFavouritesDataDto_v1 {
		return try{
			/** First try to read the input stream as an old data. if it fails, retry. if that fails,
			 *  then use the default data */
			Json.decodeFromString(ExoFavouritesDataDto_v1.serializer(), input.readBytes().decodeToString())
		}
		catch (e : Exception){
			e.printStackTrace()
			defaultValue
		}
	}

	override suspend fun writeTo(t: ExoFavouritesDataDto_v1, output: OutputStream) {
		withContext(Dispatchers.IO) {
			output.write(
				Json.encodeToString(ExoFavouritesDataDto_v1.serializer(), t).encodeToByteArray()
			)
		}
	}
}

