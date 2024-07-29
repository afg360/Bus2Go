package dev.mainhq.bus2go.viewmodels

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.fragments.FavouriteTransitInfo
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.utils.TransitAgency
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.io.BufferedReader
import java.lang.IllegalStateException
import java.net.URLEncoder.encode
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

//TODO cache or save the data somewhere in case used and disconnected
class RealTimeViewModel(application : Application) : AndroidViewModel(application) {

    private val _stmData : MutableStateFlow<FeedMessage?> = MutableStateFlow(null)
    val stmData : StateFlow<FeedMessage?> = _stmData
    private val _exoData : MutableStateFlow<FeedMessage?> = MutableStateFlow(null)
    val exoData : StateFlow<FeedMessage?> = _exoData
    private val domainName : String =
        BufferedReader(application.applicationContext.resources.openRawResource(R.raw.config).reader()).readLine()

    suspend fun loadData(){
        /* For trip updates */
        //_stmData.value = getStmData()
        //_exoData.value = getExoData()
    }

    suspend fun getData(list : List<TransitData>, agency: TransitAgency, roomViewModel: RoomViewModel) : MutableList<FavouriteTransitInfo>?{
        val mutableList= mutableListOf<FavouriteTransitInfo>()
        when(agency){
            TransitAgency.STM -> {
                val data = stmData.value
                return if (data == null) null
                else {
                    list as List<StmBusData>
                    data.entityList.forEach{
                        if (it.hasTripUpdate() && it.hasStop()) {
                            val tripUpdate = it.tripUpdate
                            if (tripUpdate.hasTrip()){
                                val trip = tripUpdate.trip
                                //make a CHUNKED list of jobs
                                val jobs = mutableListOf<Job>()
                                list.chunked(50).forEach { innerList ->
                                    jobs.add(viewModelScope.launch {
                                        innerList.forEach {
                                            favourite ->
                                            tripUpdate.stopTimeUpdateList.forEach { stopTimeUpdate ->
                                                if (/*favourite.stopName*/ "Côte-Saint-Luc / Rosedale" == roomViewModel.getNames(stopTimeUpdate.stopId.toInt())
                                                    && /*favourite.routeId*/ "103" == trip.routeId
                                                    /*&& /*favourite.directionId*/ 0 == trip.directionId*/)
                                                    mutableList.add(FavouriteTransitInfo(favourite, Time.TimeBuilder.fromUnix(stopTimeUpdate.arrival.time), agency))
                                            }
                                        }
                                    })
                                }
                                jobs.forEach { job -> job.join() }
                            }
                        }
                    }
                    mutableList
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

    private suspend fun getStmData(){

    }

    private suspend fun getExoData(){

    }

    suspend fun getDataFromServer(agency: String, routeId : String, tripHeadsign : String, stopName : String) : JsonObject?{
        val tldUrl = "$domainName:$PORT_NUM/${URL_PATH}"
        return getHttpResponse(tldUrl, agency, routeId, tripHeadsign, stopName)
    }


    companion object WebRequest {
        //FIXME this will eventually be the official bus2go domain name, for now only a test ip address
        //private lateinit var domainName : String
        const val PORT_NUM = 8000
        const val URL_PATH = "api/realtime/v1"


        private val httpClient = HttpClient(OkHttp){
            engine {
                config {
                    //FIXME this is way too long
                    readTimeout(30, TimeUnit.SECONDS)
                }
            }
        }

        suspend fun getHttpResponse(tldUrl : String, agency: String, routeId : String, tripHeadsign : String, stopName : String) : JsonObject? {
            val url = "$tldUrl/?agency=${encode(agency, "utf-8")}" +
                    "&route_id=${encode(routeId, "utf-8")}" +
                    "&trip_headsign=${encode(tripHeadsign, "utf-8")}" +
                    "&stop_name=${encode(stopName, "utf-8")}"
            return try{
                reactResponse(httpClient.get(url))
            }
            catch (e : ConnectTimeoutException){
                println("The database is probably not open...")
                null
            }
        }

        private suspend fun reactResponse(response : HttpResponse) : JsonObject? {
            when (response.status.value) {
                in 200..299 -> {
                    /**
                     * Data format:
                     * {
                     *      "transit-info":
                     *      {
                     *          "agency": str,
                     *          "route_id": str,
                     *          "trip_headsign": str,
                     *          "stop_name": str
                     *      },
                     *      "arrival_time: list[str]
                     * }
                     */
                    return Json.decodeFromString(JsonObject.serializer(), response.body())
                }
                in 300..399 -> {
                    TODO("REDIRECTION")
                }
                in 400..499 -> {
                    /**
                     * Data format:
                     * {
                     *      "detail": str
                     * }
                     */
                    return null
                    //return Json.decodeFromString(JsonObject.serializer(), response.body())
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
}