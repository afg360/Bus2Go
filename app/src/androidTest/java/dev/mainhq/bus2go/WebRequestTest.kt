package dev.mainhq.bus2go

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import com.google.transit.realtime.GtfsRealtime.TripUpdate
import dev.mainhq.bus2go.utils.web.WebRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebRequestTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var gtfs : FeedMessage
    private lateinit var tripUpdate : TripUpdate


    @Before
    fun setupWebRequest() = runBlocking {
        val t1 = async{ WebRequest.getResponse()}
        tripUpdate = WebRequest.readFromFile(context)
        gtfs = t1.await()
    }

    @Test
    fun test1() {
        val list = TripUpdate.parseFrom(gtfs.toByteArray()).stopTimeUpdateList
        //assert(list.isNotEmpty())
        //Log.d("Metadata", gtfs.header)
        //Log.d("Metadata", gtfs.header)
    }

    @Test
    fun test2(){
        Log.d("TRIPUPDATE", tripUpdate.toString())
    }
}