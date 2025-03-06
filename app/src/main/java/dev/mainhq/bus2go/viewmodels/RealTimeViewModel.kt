package dev.mainhq.bus2go.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import dev.mainhq.bus2go.preferences.ExoBusData
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.utils.TransitAgency
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder.encode
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

//TODO cache or save the data somewhere in case used and disconnected
class RealTimeViewModel(application : Application) : AndroidViewModel(application) {

    //perhaps store also the port number in the config/.env file
    //need to update this value when the user makes changes here
    private val _domainName : MutableLiveData<String> = MutableLiveData("0.0.0.0")
    private val domainName : LiveData<String> get() = _domainName
    private val _stmRealData : MutableLiveData<Map<String, List<String>>> = MutableLiveData(mapOf())
    val stmRealData: LiveData<Map<String, List<String>>> get() = _stmRealData

    fun loadDomainName(application: Application) {
        _domainName.value = PreferenceManager.getDefaultSharedPreferences(application).getString("server-choice", "0.0.0.0")!!
    }
    
    /** Make an http request to the backend server to receive the latest arrival times for a certain
     * transit info (stop name of a route).  This method also parses the data received
     **/
    @Deprecated("Only a tmp/test method for when the client was making unefficient http requests. " +
            "Use getRealTime() instead")
    suspend fun getArrivalTimes(agency: String, routeId: String, /** AKA direction */tripHeadsign: String, stopName: String) =
        NetworkClient.getArrivalTimes(domainName.value!!, agency, routeId, tripHeadsign, stopName)
    
    /**
     * Get the real time data from a bus2go server by establishing a websocket with it
     * */
    suspend fun getRealTime(data: List<StmBusData>): Int{//, exo: List<ExoBusData>, train: List<TrainData>, view: View) {
        return NetworkClient.getRealTime(domainName.value!!, data)//, exo, train, view, this)
    }

    //we could store this inside the favourite view model, and keep a list
    //of realtime data as a private attr
    /**
     * For the moment, thic companion object should only be used privately by the parent class RealTimeViewModel
     * */
    companion object NetworkClient {
        //FIXME this will eventually be the official bus2go domain name, for now only a test ip address
        //private lateinit var domainName : String
        const val PORT_NUM = 8000
        private const val API_VERSION = "v1"
        private const val URL_PATH = "api/realtime/$API_VERSION/"
        private const val TEST = "api/realtime/$API_VERSION/test"
        private val client = HttpClient(OkHttp) {
            install(WebSockets)
            engine {
                preconfigured = OkHttpClient.Builder()
                    .pingInterval(20, TimeUnit.SECONDS)
                    .build()
            }
        }
        
        /**
         * Test to see if websockets work...
         * */
        suspend fun test(domainName: String){
            //client.webSocket(method = HttpMethod.Get, host = domainName, port = PORT_NUM, path = test) {
            client.webSocket("ws://$domainName:$PORT_NUM/$TEST"){
                while (true) {
                    val myMessage = "Hello world"
                    send(myMessage)
                    val firstMessage = incoming.receive() as? Frame.Text
                    println(firstMessage?.readText())
                    val secondMessage = incoming.receive() as? Frame.Text
                    val lst : List<Test> = Json.decodeFromString(Foo.serializer(), secondMessage!!.readText()).response
                    lst.forEach {
                        println("id: ${it.pId}, message: ${it.myMessage}, lst: ${it.lst}")
                    }
                    //attempting deserialisation of json data
                    delay(5000)
                }
            }
            client.close()
        }
        
        suspend fun getRandom(domainName: String){
            client.webSocket("ws://$domainName:$PORT_NUM/$TEST/random") {
                while(true){
                    val message = incoming.receive() as? Frame.Text
                    println(message?.readText())
                    val ghol = Json.decodeFromString<Response>(message!!.readText())
                    println(ghol)
                }
            }
        }
        
        /**
         * A method use to establish a websocket connection with the server
         * For the moment only accept list of stmbusdata */
        suspend fun getRealTime(domainName: String, list: List<StmBusData>): Int{//, exo: List<ExoBusData>, train: List<TrainData>,
                               //view: View, viewModel : RealTimeViewModel){
            
            try{
                client.webSocket("ws://$domainName:$PORT_NUM/$URL_PATH"){
                    while (true){
                        val request = list.map{
                            Json.encodeToJsonElement(TransitInfo(TransitAgency.STM, it.routeId, it.direction, it.stopName))
                        }
                        println(JsonArray(request).toString())
                        send(JsonArray(request).toString())
                        //TODO ensure that data exchange is not fragmented!
                        val othersMessage = incoming.receive() as? Frame.Text
                        //check error status and response
                        othersMessage?.also {
                            val response = Json.decodeFromString<Response>(it.readText())
                            println("Data received: $response")
                            
                            //viewModel._stmRealData.value = data
                            //FIXME change or add a new field for favourite view models instead, when done with parsing
                        }
                        //task(view, othersMessage, true)
                        delay(5000)
                    }
                }
                client.close()
                return 0
            }
            catch (se: SocketTimeoutException){
                println("Could not connect to the server. Perhaps the server is not running... or does not accept connections")
                println(se.message)
                client.close()
                return 1
            }
            catch (e: ConnectException){
                println("There was an error trying to connect to the server")
                println(e.message)
                client.close()
                return 1
            }
        }
        
        /**
         * A test/tmp method that establishes an http request
         * */
        suspend fun getArrivalTimes(domainName: String, agency: String,
            routeId: String, tripHeadsign: String, stopName: String) :List<Time> {
            //FIXME temporarily HTTP instead of HTTPS
            val url = "http://$domainName:$PORT_NUM/$URL_PATH/?agency=${encode(agency, "utf-8")}" +
                    "&route_id=${encode(routeId, "utf-8")}" +
                    "&trip_headsign=${encode(tripHeadsign, "utf-8")}" +
                    "&stop_name=${encode(stopName, "utf-8")}"
            return try {
                val response = client.get(url)
                when (response.status.value) {
                    in 200..299 -> {
                        /**
                         * Data format:
                         * listOf {
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
                        return (Json.decodeFromString(JsonObject.serializer(),
                                response.body() )["arrival_time"] as JsonArray?)
                                ?.filter{it.toString() != ""}
                                //?.map{ Time.TimeBuilder.fromUnix(it.toString().toLong()) }
                                ?.map{ Time.fromUnix(it.toString().toLong()) }
                                ?: listOf()
                    }
                    
                    in 300..399 -> {
                        TODO("REDIRECTION")
                    }
                    
                    in 400..499 -> {
                        /**
                         * Data format:  { "detail": str }
                         */
                        return listOf()
                    }
                    
                    in 500..599 -> {
                        Log.e("Server side error", Json.decodeFromString(JsonObject.serializer(), response.body()).toString())
                        return listOf()
                    }
                    
                    else -> {
                        throw ConnectTimeoutException("There seems to be no internet connection for the http request to be sent with")
                    }
                }
            }
            catch (e: ConnectTimeoutException) {
                Log.e("Timeout Error", "The database is probably not open...")
                listOf()
            }
            
            catch (e: ConnectException){
                Log.e("Connection Error", "Could not properly connect to the ip address: $url\nPerhaps the firewall of the server is blocking your connection")
                listOf()
            }
            catch (e: IOException){
                Log.e("IO Exception", "The stream received has been corrupted. Perhaps an interruption occurred to the connection")
                listOf()
            }
        }
    }
}

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

//object Foo : JsonTransformingSerializer<TransitInfo>(TransitInfo.serializer())

@Serializable
data class Foo(val response: List<Test>)

@Serializable
data class Test(
    @SerialName("id")
    val pId: Int,
    @SerialName("message")
    val myMessage: String, val lst: List<String>)

