package dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity

import android.annotation.SuppressLint
import dev.mainhq.bus2go.data.data_source.local.datastore.PersistentTagListSerializer
import dev.mainhq.bus2go.data.data_source.local.datastore.TransitDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagDto
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@SuppressLint("UnsafeOptInUsageError")
@Parcelize
@Serializable
/**
 * @param routeId aka busNum.
 **/
data class StmFavouriteBusItemDto(
	override val stopName: String,
	override val routeId : String,
	override val direction : String,
	@Serializable(with = PersistentTagListSerializer::class)
	override val tags: PersistentList<TagDto>,
	val directionId: Int,
	val lastStop : String,
) : TransitDataDto()


class PersistentStmBusInfoListSerializer(private val serializer: KSerializer<StmFavouriteBusItemDto>) :
	KSerializer<PersistentList<StmFavouriteBusItemDto>> {

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<PersistentList<StmFavouriteBusItemDto>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()

	override fun serialize(encoder: Encoder, value: PersistentList<StmFavouriteBusItemDto>) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<StmFavouriteBusItemDto> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}

