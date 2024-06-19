package dev.mainhq.bus2go

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.transit.realtime.GtfsRealtime.FeedEntity
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import com.google.transit.realtime.GtfsRealtime.TripUpdate
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class WebRequestTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var data : ByteArray
    private lateinit var entityList : List<FeedEntity>

    @Before
    fun setupWebRequest() {
        data = context.resources.openRawResource(R.raw.stm).readBytes()
    }

    @Test
    fun nonEmptyData(){
        entityList = FeedMessage.parseFrom(data).entityList
        assert(entityList.isNotEmpty())
    }

    @Test
    fun checkingData(){
        entityList = FeedMessage.parseFrom(data).entityList
        assert(entityList[51].hasTripUpdate())
        var i = 0
        entityList.forEach {feedEntity ->
            //search for tripId instead of routeId, and get stopSeq or stopId or both?
            if (feedEntity.tripUpdate.trip.tripId == "275633760"){
                feedEntity.tripUpdate.stopTimeUpdateList.forEach {
                    if (it.stopId == "56409"){
                        Log.d("STOPTimes", it.toString())

                    }
                }
            }
        }
    }
}