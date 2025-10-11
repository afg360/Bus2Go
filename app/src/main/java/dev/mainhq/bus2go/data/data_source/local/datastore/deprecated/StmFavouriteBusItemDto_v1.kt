package dev.mainhq.bus2go.data.data_source.local.datastore.deprecated

import android.annotation.SuppressLint
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.TransitDataDto_v2
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
@Deprecated("Use version 2")
/**
 * @param routeId aka busNum.
 **/
data class StmFavouriteBusItemDto_v1(
	override val stopName: String,
	override val routeId : String,
	val directionId: Int,
	override val direction : String,
	val lastStop : String
) : TransitDataDto_v2()


@Deprecated("Use newer version")
class PersistentStmBusInfoListSerializer_v1(private val serializer: KSerializer<StmFavouriteBusItemDto_v1>) :
	KSerializer<PersistentList<StmFavouriteBusItemDto_v1>> {

	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<List<StmFavouriteBusItemDto_v1>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}

	override val descriptor: SerialDescriptor = PersistentListDescriptor()

	override fun serialize(encoder: Encoder, value: PersistentList<StmFavouriteBusItemDto_v1>) {
		return ListSerializer(serializer).serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): PersistentList<StmFavouriteBusItemDto_v1> {
		return ListSerializer(serializer).deserialize(decoder).toPersistentList()
	}
}

