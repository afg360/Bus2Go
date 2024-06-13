package dev.mainhq.bus2go

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.database.stm_data.dao.RoutesDAO
import dev.mainhq.bus2go.database.stm_data.dao.TripsDAO
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var tripsDao: TripsDAO
    private lateinit var routesDao: RoutesDAO
    //private lateinit var stoptimesDao : StopTimesDAO
    private lateinit var db: AppDatabaseSTM

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabaseSTM::class.java).build()
        tripsDao = db.tripsDao()
        routesDao = db.routesDao()
        //stoptimesDao = db.stopTimesDao()
    }

    @Test
    fun nonEmptyTrips() = runBlocking {
        val res = tripsDao.getDirectionInfo(6)
        Log.d("Res", res.toString())
        assert(res.isNotEmpty())
    }

    @Test
    fun nonEmptyRoutes() = runBlocking {
        val dirs = routesDao.getBusDir()
        Log.d("Res", dirs.toString())
        assert(dirs.isNotEmpty())
    }

    /*@Test
    fun test(): Unit = runBlocking {
        val stops = stoptimesDao.getStopInfoFromBusNum("103-E")
        Log.d("TEST STOPS", stops.toString())
        assert(stops.isNotEmpty())
    }*/

    @After
    fun closeDb() {
        db.close()
    }
}