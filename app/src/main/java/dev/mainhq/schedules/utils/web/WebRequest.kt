package dev.mainhq.schedules.utils.web

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import com.google.transit.realtime.GtfsRealtime.TripUpdate
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

object WebRequest {
    const val URL = "https://api.stm.info/pub/od/gtfs-rt/ic/v2"
    const val timesURL = "$URL/tripUpdates"
    const val positionsURL = "$URL/vehiclePositions"
    //TODO TO REMOVE API KEY
    private const val apiKey = ""

    suspend fun getResponse() {
        val client = HttpClient(OkHttp){}
        val httpResponse: HttpResponse = client.get(timesURL) {
            headers {
                append("apiKey", apiKey)
                append(HttpHeaders.ContentType, "application/x-protobuf")
            }
        }
        when (httpResponse.status.value) {
            in 200..299 -> {
                val body : ByteArray = httpResponse.body()
                val gtfs : MutableList<StopTimeUpdate> = TripUpdate.parseFrom(body).stopTimeUpdateList
                val hashMap = HashMap<Pair<String, Int>, Long>()
                gtfs.forEach {
                    val stopId = it.stopId
                    val arrival = it.arrival.time
                    val stopSequence = it.stopSequence
                    Log.d("STOPID", stopId.toString())
                    Log.d("STOPSEQ", stopSequence.toString())
                    Log.d("ARRIVAL", "$arrival\n\n")
                    hashMap[Pair(stopId, stopSequence)] = arrival
                }

                return
            }
            in 300..399 -> {
                TODO("REDIRECTION")
            }
            in 400..499 -> {
                TODO("PAGE DOESNT EXIST / BAD REQUEST")
            }
            in 500..599 -> {
                TODO("SERVERSIDE EXCEPTION")
            }
            else -> {
                TODO("TIMEOUT EXCEPTION")
            }
        }
    }
}