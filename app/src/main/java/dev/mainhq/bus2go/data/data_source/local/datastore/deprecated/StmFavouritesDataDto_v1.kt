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


@SuppressLint("UnsafeOptInUsageError")
@Serializable
@Deprecated("Use version 2, which contains tags")
data class StmFavouritesDataDto1(
	val version: Int,
	@Serializable(with = PersistentStmBusInfoListSerializer_v1::class)
	val listSTM : PersistentList<StmFavouriteBusItemDto_v1> = persistentListOf()
)

@Deprecated("")
object StmFavouritesDataSerializer1 : Serializer<StmFavouritesDataDto1> {
	override val defaultValue: StmFavouritesDataDto1
		get() = StmFavouritesDataDto1(1)

	override suspend fun readFrom(input: InputStream): StmFavouritesDataDto1 {
		return try{
			/** First try to read the input stream as an old data. if it fails, retry. if that fails,
			 *  then use the default data */
			Json.decodeFromString(StmFavouritesDataDto1.serializer(), input.readBytes().decodeToString())
		}
		catch (e : Exception){
			e.printStackTrace()
			defaultValue
		}
	}

	override suspend fun writeTo(t: StmFavouritesDataDto1, output: OutputStream) {
		withContext(Dispatchers.IO) {
			output.write(
				Json.encodeToString(StmFavouritesDataDto1.serializer(), t).encodeToByteArray()
			)
		}
	}
}

