package dev.mainhq.schedules

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.database.dao.RoutesDAO
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
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        tripsDao = db.tripsDao()
        routesDao = db.routesDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun nonEmptyTrips() = runBlocking {
        val res = tripsDao.getTripHeadsigns()
        Log.d("Res", res.toString())
        assert(res.isNotEmpty())
        //assertThat(res.get(0), equalTo("5"))
    }

    @Test
    @Throws(Exception::class)
    fun nonEmptyRoutes() = runBlocking {
        val dirs = routesDao.getBusDir()
        Log.d("Res", dirs.toString())
        assert(dirs.isNotEmpty())
        //assertThat(dirs.get(0), equalTo("Wrong test"))
    }
}