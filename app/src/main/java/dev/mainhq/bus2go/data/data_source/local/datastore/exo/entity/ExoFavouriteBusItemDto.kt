package dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity

import dev.mainhq.bus2go.data.data_source.local.datastore.TransitDataDto
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

@Parcelize
@Serializable
data class ExoFavouriteBusItemDto(
	override val stopName : String,
	override val routeId : String,
	override val direction: String,
	val routeLongName: String,
	val headsign: String
) : TransitDataDto()

class PersistentExoBusInfoListSerializer(private val serializer: KSerializer<ExoFavouriteBusItemDto>) :
	KSerializer<PersistentList<ExoFavouriteBusItemDto>> {

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<List<ExoFavouriteBusItemDto>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()

	override fun serialize(encoder: Encoder, value: PersistentList<ExoFavouriteBusItemDto>) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<ExoFavouriteBusItemDto> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}
