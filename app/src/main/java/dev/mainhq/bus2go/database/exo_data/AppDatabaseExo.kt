package dev.mainhq.bus2go.database.exo_data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.mainhq.bus2go.database.Converters
import dev.mainhq.bus2go.database.exo_data.dao.*
import dev.mainhq.bus2go.database.exo_data.entities.*

@Database(entities = [Routes::class, Trips::class, StopTimes::class,
    Stops::class, Calendar::class, CalendarDates::class, Shapes::class, Forms::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabaseExo : RoomDatabase() {
    abstract fun routesDao() : RoutesDAO
    abstract fun tripsDao() : TripsDAO
    abstract fun stopDao() : StopsDAO
    abstract fun stopTimesDao() : StopTimesDAO
    abstract fun calendarDao() : CalendarDAO
    abstract fun calendarDatesDAO() : CalendarDatesDAO
    abstract fun shapesDao() : ShapesDAO
    abstract fun formsDao() : FormsDAO

    companion object {
        val MIGRATION_1_2 = object: Migration(1, 2){
            override fun migrate(db: SupportSQLiteDatabase) {
                //TODO("Not implemented")
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
