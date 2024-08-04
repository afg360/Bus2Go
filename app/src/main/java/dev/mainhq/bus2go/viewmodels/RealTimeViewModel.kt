package dev.mainhq.bus2go.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.utils.Time
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.get
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.io.BufferedReader
import java.io.IOException
import java.net.ConnectException
import java.net.URLEncoder.encode
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

//TODO cache or save the data somewhere in case used and disconnected
class RealTimeViewModel(application : Application) : AndroidViewModel(application) {

    //perhaps store also the port number in the config/.env file
    //need to update this value when the user makes changes here
    private val _domainName : MutableLiveData<String> = MutableLiveData("0.0.0.0")
    private val domainName : LiveData<String> get() = _domainName
        //BufferedReader(application.applicationContext.resources.openRawResource(R.raw.config).reader()).readLine()

    fun loadDomainName(application: Application) {
        _domainName.value = PreferenceManager.getDefaultSharedPreferences(application).getString("server-choice", "0.0.0.0")!!
    }
    
    /** Make an http request to the backend server to receive the latest arrival times for a certain
     * transit info (stop name of a route).  This method also parses the data received
     **/
    
    suspend fun getArrivalTimes(agency: String, routeId: String, /** AKA direction */tripHeadsign: String, stopName: String) =
        NetworkClient.getArrivalTimes(domainName.value!!, agency, routeId, tripHeadsign, stopName)

    //we could store this inside the favourite view model, and keep a list
    //of realtime data as a private attr
    companion object NetworkClient {
        //FIXME this will eventually be the official bus2go domain name, for now only a test ip address
        //private lateinit var domainName : String
        const val PORT_NUM = 8000
        private const val URL_PATH = "api/realtime/v1"
        private val httpClient = HttpClient(OkHttp) {
            engine {
                config {
                    //readTimeout(10, TimeUnit.SECONDS)
                }
            }
        }
        
        suspend fun getArrivalTimes(domainName: String, agency: String,
            routeId: String, tripHeadsign: String, stopName: String ): List<Time> {
            //FIXME temporarily HTTP instead of HTTPS
            val url = "http://$domainName:$PORT_NUM/$URL_PATH/?agency=${encode(agency, "utf-8")}" +
                    "&route_id=${encode(routeId, "utf-8")}" +
                    "&trip_headsign=${encode(tripHeadsign, "utf-8")}" +
                    "&stop_name=${encode(stopName, "utf-8")}"
            return try {
                val response = httpClient.get(url)
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
                        return (Json.decodeFromString( JsonObject.serializer(),
                                response.body() )["arrival_time"] as JsonArray?)
                                ?.filter{it.toString() != ""}
                                ?.map{ Time.TimeBuilder.fromUnix(it.toString().toLong()) }
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