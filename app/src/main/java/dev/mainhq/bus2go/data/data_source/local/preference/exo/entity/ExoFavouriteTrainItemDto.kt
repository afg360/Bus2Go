package dev.mainhq.bus2go.data.data_source.local.preference.exo.entity

import dev.mainhq.bus2go.data.data_source.local.preference.TransitDataDto
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ExoFavouriteTrainItemDto(
	override val stopName : String,
	override val routeId : String,
	val trainNum : Int,
	val routeName : String,
	val directionId: Int,
	override val direction : String
) : TransitDataDto()


class PersistentTrainInfoListSerializer(private val serializer: KSerializer<ExoFavouriteTrainItemDto>) :
	KSerializer<PersistentList<ExoFavouriteTrainItemDto>> {

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<List<ExoFavouriteTrainItemDto>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()

	override fun serialize(encoder: Encoder, value: PersistentList<ExoFavouriteTrainItemDto>) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<ExoFavouriteTrainItemDto> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}
