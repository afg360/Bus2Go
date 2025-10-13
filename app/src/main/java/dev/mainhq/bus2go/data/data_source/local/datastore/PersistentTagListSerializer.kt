package dev.mainhq.bus2go.data.data_source.local.datastore

import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagDto
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** A basic serializer for PersistentList of Strings */
class PersistentTagListSerializer(private val serializer: KSerializer<TagDto>)
	: KSerializer<PersistentList<TagDto>>{

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<PersistentList<TagDto>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()
	override fun serialize(
		encoder: Encoder,
		value: PersistentList<TagDto>,
	) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<TagDto> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}