package dev.mainhq.bus2go.data.data_source.remote.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Response(val response: List<JsonTransitTime>)

@Serializable
data class JsonTransitTime(
	@SerialName("transit_info")
	val transitInfo: TransitInfo,
	@SerialName("arrival_time")
	//val arrivalTime: List<String>
	//tmp make it ints instead
	val arrivalTime: List<Int>
)

@Serializable
data class TransitInfo(val agency: TransitAgency,
					   @SerialName("route_id")
					   val routeId: String,
					   @SerialName("trip_headsign")
					   val tripHeadsign: String,
					   @SerialName("stop_name")
					   val stopName: String
)

@Serializable
data class Foo(val response: List<Test>)

@Serializable
data class Test(
	@SerialName("id")
	val pId: Int,
	@SerialName("message")
	val myMessage: String, val lst: List<String>)
