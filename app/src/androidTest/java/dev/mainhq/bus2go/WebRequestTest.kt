package dev.mainhq.bus2go

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.transit.realtime.GtfsRealtime.FeedEntity
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import com.google.transit.realtime.GtfsRealtime.TripUpdate
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.viewmodels.RealTimeViewModel
import io.ktor.client.call.body
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import org.json.JSONStringer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class WebRequestTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var data : ByteArray
    private lateinit var entityList : List<FeedEntity>
    private val favourites = listOf(
        Answer(
            StmBusData(
                "Côte-des-Neiges / Jean-Talon",
                "165",
                0,
                "Sud",
                "foo"
            ),
            404
        ),
        Answer(
            StmBusData(
                "Côte-des-Neiges / Mackenzie",
                "103",
                0,
                "Nord",
                "foo"
            ),
            404
        ),
        Answer(
            StmBusData(
                "de Monkland / Royal",
                "103",
                0,
                "Est",
                "foo"
            ),
            200
        ),
        Answer(
            StmBusData(
                "Côte-des-Neiges / Jean-Talon",
                "987tygh",
                0,
                "Est",
                "foo"
            ),
            418
        ),
        Answer(
            StmBusData(
                "Côte-des-Neiges / Jean-Talon",
                "3jirf'`\"",
                0,
                "Sud",
                "foo"
            ),
            418
        )
    )

    //Used for testing parsing of gtfs-realtime protobuffer
    //@Before
    //fun setupWebRequest() {
    //    data = context.resources.openRawResource(R.raw.stm).readBytes()
    //}

    //Need to read the raw/config file to get the proper domain name
    /*
    @Test
    fun localWebRequest(){
        //e.g. data inside favourites
        runBlocking {
            val jobs = favourites.map {
                Pair(
                    it,
                    async {
                        RealTimeViewModel.getHttpResponse(
                            "http://localhost:8000", "STM", it.data.routeId, it.data.direction, it.data.stopName)
                    }
                )
            }
            val results = jobs.map { Pair(it.first, it.second.await()) }
            results.forEach { (it.second?.get("arrival_time") as JsonArray?)?.forEach {
                Log.d("TIMEDATA", Time.TimeBuilder.fromUnix(it.toString().toLong()).toString()) }
            }
        }
    }
    */

    private data class Answer(val data: StmBusData, val expectedStatusCode: Int)
}