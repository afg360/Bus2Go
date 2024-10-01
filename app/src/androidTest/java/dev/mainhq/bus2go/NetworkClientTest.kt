package dev.mainhq.bus2go

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.transit.realtime.GtfsRealtime.FeedEntity
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.viewmodels.RealTimeViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkClientTest {

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

    @Test
    fun testWebSocket(){
        runBlocking {
            RealTimeViewModel.NetworkClient.test("0.0.0.0")
        }
    }
    
    @Test
    fun testRealTime(){
        runBlocking {
            RealTimeViewModel.NetworkClient.getRealTime("0.0.0.0", "STM", "103",
                "Ouest", "Côte-Saint-Luc / Hudson") {
                
            }
        }
    }
    
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
                        RealTimeViewModel.NetworkClient.getArrivalTimes(
                            "localhost", "STM", it.data.routeId, it.data.direction, it.data.stopName)
                    }
                )
            }
            //val results = jobs.map { Pair(it.first, it.second.await()) }
            //results.forEach { it.second.forEach {
            //    Log.d("TIMEDATA", Time.TimeBuilder.fromUnix(it.toString().toLong()).toString()) }
            //}
        }
        
    }
     */

    private data class Answer(val data: StmBusData, val expectedStatusCode: Int)
}