package dev.mainhq.bus2go.data.data_source.local.database.stm;

import android.content.Context
import android.util.Log
import androidx.room.Database;
import androidx.room.Room
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.mainhq.bus2go.data.data_source.local.Converters
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDatesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.FormsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.ShapesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsInfoDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.TripsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Calendar
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.CalendarDates
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Forms
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Routes
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Shapes
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Stops
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.StopsInfo
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Trips
import java.io.IOException

@Database(
    entities = [Routes::class, Trips::class, StopsInfo::class,
        Stops::class, Calendar::class, CalendarDates::class, Shapes::class, Forms::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabaseSTM : RoomDatabase() {
    abstract fun routesDao() : RoutesDAO
    abstract fun tripsDao() : TripsDAO
    abstract fun stopsInfoDao() : StopsInfoDAO
    abstract fun stopDao() : StopsDAO
    abstract fun calendarDao() : CalendarDAO
    abstract fun calendarDatesDao() : CalendarDatesDAO
    abstract fun shapesDao() : ShapesDAO
    abstract fun formsDao() : FormsDAO

    companion object {
        const val DATABASE_NAME = "stm_data.db"
        const val DATABASE_PATH = "database/$DATABASE_NAME"
        private var INSTANCE: AppDatabaseSTM? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabaseSTM? {
            return INSTANCE ?: createDatabase(context).also { INSTANCE = it }
        }

        private fun createDatabase(context: Context): AppDatabaseSTM? {
            val dbFile = context.getDatabasePath(DATABASE_NAME)

            //FIXME this is a hack, better checks need to be performed to determine the correct
            // db to read
            try {
                //from downloads if downloaded
                return if (dbFile.exists() && dbFile.length() > 0)
                    Room.databaseBuilder(context, AppDatabaseSTM::class.java, DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .build()
                //FIXME does it work even if bundled...?
                else if (context.assets.list("database")?.contains("stm_info.db") == true)
                    //from assets if bundled
                    Room.databaseBuilder(context, AppDatabaseSTM::class.java, DATABASE_NAME)
                        .createFromAsset(DATABASE_PATH)
                        .addMigrations(MIGRATION_1_2)
                        .build()
                else null
            }
            catch (ioe: IOException){
                //FIXME shouldnt have that logcat here but is convenient @ the moment...
                Log.e("DATABASES", "STM Database not found...")
                return null
            }
        }

        //FIXME Needs to be called when db is being updated...
        fun invalidateInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }

        //we will add a calendar_dates table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                //changes:
                //  added a table CalendarDates
                //  got rid of ID in calendar, primary key is now service_id
                //  must change references to calendar?
                //need to recreate table Calendar, by copying, dropping old, and renaming new
                db.execSQL("CREATE TABLE IF NOT EXISTS Copy(" +
                        "service_id TEXT PRIMARY KEY NOT NULL," +
                        "days TEXT NOT NULL," +
                        "start_date INTEGER NOT NULL," +
                        "end_date INTEGER NOT NULL)")
                db.execSQL("INSERT INTO Copy (service_id, days, start_date, end_date) SELECT service_id, days, start_date, end_date FROM Calendar")
                db.execSQL("DROP TABLE Calendar")
                db.execSQL("ALTER TABLE Copy RENAME to Calendar")

                db.execSQL("CREATE TABLE IF NOT EXISTS CalendarDates (" +
                        "service_id TEXT NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "exception_type INTEGER NOT NULL, " +
                        "PRIMARY KEY (service_id, date), " +
                        "FOREIGN KEY(service_id) REFERENCES Calendar(service_id))")
            }
        }
    }
}
