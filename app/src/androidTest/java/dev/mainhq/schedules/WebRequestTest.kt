package dev.mainhq.schedules

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.mainhq.schedules.utils.web.WebRequest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebRequestTest {

    @Test
    fun httpRequest(): Unit = runBlocking {
        /*
        val foo : ByteArray = WebRequest.getResponse()
        Log.d("RESPONSE", foo.toString())
        Log.d("SIZE", foo.size.toString())
        assert(foo.size > 50)

         */
    }
}