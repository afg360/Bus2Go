package dev.mainhq.bus2go.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.transit.realtime.GtfsRealtime
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.fragments.FavouriteTransitInfo
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.TransitAgency
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.lang.IllegalStateException
import java.util.concurrent.TimeoutException

//TODO cache or save the data somewhere in case used and disconnected
class RealTimeViewModel(application : Application) : AndroidViewModel(application) {
    private lateinit var EXO_API_KEY: String
    private lateinit var STM_API_KEY : String
    private val _stmData : MutableStateFlow<FeedMessage?> = MutableStateFlow(null)
    val stmData : StateFlow<FeedMessage?> = _stmData
    private val _exoData : MutableStateFlow<FeedMessage?> = MutableStateFlow(null)
    val exoData : StateFlow<FeedMessage?> = _exoData

    init{
        application.resources.openRawResource(R.raw.config).bufferedReader().lines().forEach {
            val line = it.split("=", limit = 2)
            when(line[0]) {
                "stm_token" -> {
                    STM_API_KEY = line[1]
                }
                "exo_token" -> {
                    EXO_API_KEY = line[1]
                }
                else -> throw IllegalStateException("An invalid line has been written to the raw config file!")
            }
        }
        if (EXO_API_KEY.isEmpty() || STM_API_KEY.isEmpty()) throw IllegalStateException("One of the api keys has not been given for real time data!")
    }

    suspend fun loadData(){
        /* For trip updates */
        _stmData.value = getStmData()
        _exoData.value = getExoData()
    }

    fun getData(list : List<TransitData>, agency: TransitAgency) : MutableList<FavouriteTransitInfo>?{
        val mutableList= mutableListOf<FavouriteTransitInfo>()
        when(agency){
            TransitAgency.STM -> {
                val data = stmData.value
                return if (data == null) null
                else {
                    val entities = data.entityList
                    entities.forEach{
                        if (it.hasTripUpdate() && it.hasStop()) {
                            val tripUpdate = it.tripUpdate
                            val stop = it.stop
                            if (tripUpdate.hasTrip()){
                                val trip = tripUpdate.trip
                                if (list.contains(StmBusData(stop.stopName.toString(), trip.routeId.toInt(), trip.directionId,
                                    /* put the last direction */"", ""))){
                                    println("TRIP: $trip, STOP: $stop")
                                }
                                if (trip.routeId == "103")
                                    println("TRIP: $trip, STOP: $stop")
                            }
                        }
                        /*
                        if (it.hasAlert()) {
                            println(it.alert)
                        }
                        if (it.hasVehicle()) {
                            println(it.vehicle)
                        }
                         */
                    }
                    return mutableList
                }
            }
            TransitAgency.EXO_OTHER -> {
                return mutableList
            }
            TransitAgency.EXO_TRAIN -> {
                return mutableList
            }
        }
    }

    private suspend fun getStmData() : FeedMessage{
        return try {
            WebRequest.getResponse("${WebRequest.STM_URL}/tripUpdates") {
                headers {
                    append("apiKey", STM_API_KEY)
                    append(HttpHeaders.ContentType, "application/x-protobuf")
                }
            }
            /* For positions
                *  const val positionsURL = "$STM_URL/vehiclePositions"
                * */
        } catch (e: TimeoutException) {
            TODO("Not implemented yet")
        }
    }

    private suspend fun getExoData() : FeedMessage{
        try {
            /* For trip updates, don't forget to add agency */
            return WebRequest.getResponse("${WebRequest.EXO_URL}/TripUpdate.pb?token=$EXO_API_KEY")
        }
        catch (e : TimeoutException){
            TODO("")
        }
    }

    private object WebRequest {
        const val STM_URL = "https://api.stm.info/pub/od/gtfs-rt/ic/v2"
        const val EXO_URL = "https://opendata.exo.quebec/ServiceGTFSR"

        suspend fun getResponse(url : String) : FeedMessage{
            return reactResponse(HttpClient(OkHttp).get(url))
        }

        suspend fun getResponse(url : String, requestBuilder: HttpRequestBuilder.() -> Unit) : FeedMessage {
            return reactResponse(HttpClient(OkHttp).get(url, requestBuilder))
        }

        private suspend fun reactResponse(response : HttpResponse) : FeedMessage{
            when (response.status.value) {
                in 200..299 -> {
                    val body : ByteArray = response.body()
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
    }

    /** For testing purposes */
    suspend fun readFromFile(context : Context, file : Int) : FeedMessage {
        return coroutineScope {
            FeedMessage.parseFrom(context.resources.openRawResource(file).readBytes())
        }
    }
}