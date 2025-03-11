package dev.mainhq.bus2go

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.datastore.core.IOException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.mainhq.bus2go.data.data_source.local.exo.AppDatabaseSTM
import dev.mainhq.bus2go.data.data_source.local.stm.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.stm.dao.TripsDAO
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileOutputStream
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var tripsDao: TripsDAO
    private lateinit var routesDao: RoutesDAO
    //private lateinit var stoptimesDao : StopTimesDAO
    private lateinit var db: AppDatabaseSTM

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val dbFile = context.getDatabasePath("stm_info.db")
        if (!dbFile.exists()) {
            try {
                // Open the database from assets
                val assetManager: AssetManager = context.assets
                val inputStream: InputStream = assetManager.open("database/stm_info.db")

                // Create a FileOutputStream to the app's internal storage location
                val outputStream = FileOutputStream(dbFile)

                // Copy the contents from assets to the app's internal storage
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        db = Room.inMemoryDatabaseBuilder(context, AppDatabaseSTM::class.java)
            .createFromAsset("database/stm_info.db")
            .addMigrations(AppDatabaseSTM.MIGRATION_1_2)
            .build()
        tripsDao = db.tripsDao()
        routesDao = db.routesDao()
        //stoptimesDao = db.stopTimesDao()
    }

    @Test
    fun testMigration1to2() = runBlocking{
        val calendarDatesDao = db.calendarDatesDao()
        println(calendarDatesDao.getAllCalendarDates())
        println(routesDao.getBusDir())
        //println(db.calendarDao().getAllCalendarInfo())
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