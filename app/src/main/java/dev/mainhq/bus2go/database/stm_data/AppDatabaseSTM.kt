package dev.mainhq.bus2go.database.stm_data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.mainhq.bus2go.database.Converters
import dev.mainhq.bus2go.database.stm_data.dao.CalendarDAO
import dev.mainhq.bus2go.database.stm_data.dao.CalendarDatesDAO
import dev.mainhq.bus2go.database.stm_data.dao.FormsDAO
import dev.mainhq.bus2go.database.stm_data.dao.RoutesDAO
import dev.mainhq.bus2go.database.stm_data.dao.ShapesDAO
import dev.mainhq.bus2go.database.stm_data.dao.StopsDAO
import dev.mainhq.bus2go.database.stm_data.dao.StopsInfoDAO
import dev.mainhq.bus2go.database.stm_data.dao.TripsDAO
import dev.mainhq.bus2go.database.stm_data.entities.Calendar
import dev.mainhq.bus2go.database.stm_data.entities.CalendarDates
import dev.mainhq.bus2go.database.stm_data.entities.Forms
import dev.mainhq.bus2go.database.stm_data.entities.Routes
import dev.mainhq.bus2go.database.stm_data.entities.Shapes
import dev.mainhq.bus2go.database.stm_data.entities.Stops
import dev.mainhq.bus2go.database.stm_data.entities.StopsInfo
import dev.mainhq.bus2go.database.stm_data.entities.Trips

@Database(entities = [Routes::class, Trips::class, StopsInfo::class,
    Stops::class, Calendar::class, CalendarDates::class, Shapes::class, Forms::class], version = 2)
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
        //we will add a calendar_dates table
        val MIGRATION_1_2 = object : Migration(1, 2) {
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
