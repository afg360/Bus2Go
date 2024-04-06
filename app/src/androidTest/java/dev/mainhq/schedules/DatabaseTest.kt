package dev.mainhq.schedules

import android.content.Context
import android.util.Log
import androidx.media3.test.utils.TestUtil
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.database.dao.RoutesDAO
import dev.mainhq.schedules.database.dao.StopTimesDAO
import dev.mainhq.schedules.database.dao.TripsDAO
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var tripsDao: TripsDAO
    private lateinit var routesDao: RoutesDAO
    private lateinit var stoptimesDao : StopTimesDAO
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        tripsDao = db.tripsDao()
        routesDao = db.routesDao()
        stoptimesDao = db.stopTimesDao()
    }

    @Test
    fun nonEmptyTrips() = runBlocking {
        val res = tripsDao.getTripHeadsigns(6)
        Log.d("Res", res.toString())
        assert(res.isNotEmpty())
    }

    @Test
    fun nonEmptyRoutes() = runBlocking {
        val dirs = routesDao.getBusDir()
        Log.d("Res", dirs.toString())
        assert(dirs.isNotEmpty())
    }

    @Test
    fun test(): Unit = runBlocking {
        val stops = stoptimesDao.getArrivalTimesFromBusNumNow("103-E")
        Log.d("TEST STOPS", stops.toString())
        assert(stops.isNotEmpty())
    }

    @After
    fun closeDb() {
        db.close()
    }
}