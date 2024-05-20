package dev.mainhq.bus2go.utils.web

import android.content.Context
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import com.google.transit.realtime.GtfsRealtime.TripUpdate
import com.google.transit.realtime.GtfsRealtime.TripUpdate.parseFrom
import dev.mainhq.bus2go.R
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeoutException

object WebRequest {
    const val URL = "https://api.stm.info/pub/od/gtfs-rt/ic/v2"
    const val timesURL = "$URL/tripUpdates"
    const val positionsURL = "$URL/vehiclePositions"
    //TODO REMOVE API KEY
    private const val apiKey = ""

    suspend fun getResponse() : FeedMessage {
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
                return FeedMessage.parseFrom(body)
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
                throw TimeoutException("There seems to be no internet connection for the http request to be sent with")
            }
        }
    }

    suspend fun readFromFile(context : Context) : TripUpdate{
        return coroutineScope {
            TripUpdate.parseFrom(context.resources.openRawResource(R.raw.response).readBytes())
        }
    }
}