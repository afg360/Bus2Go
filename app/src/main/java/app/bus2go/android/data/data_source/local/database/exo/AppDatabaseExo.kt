package app.bus2go.android.data.data_source.local.database.exo;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.bus2go.android.data.data_source.local.database.exo.dao.CalendarDAO
import app.bus2go.android.data.data_source.local.database.exo.dao.CalendarDatesDAO
import app.bus2go.android.data.data_source.local.database.exo.dao.FormsDAO
import app.bus2go.android.data.data_source.local.database.exo.dao.RoutesDAO
import app.bus2go.android.data.data_source.local.database.exo.dao.ShapesDAO
import app.bus2go.android.data.data_source.local.database.exo.dao.StopTimesDAO
import app.bus2go.android.data.data_source.local.database.exo.dao.StopsDAO
import app.bus2go.android.data.data_source.local.database.exo.dao.TripsDAO
import app.bus2go.android.data.data_source.local.database.exo.entity.Calendar
import app.bus2go.android.data.data_source.local.database.exo.entity.CalendarDates
import app.bus2go.android.data.data_source.local.database.exo.entity.Forms
import app.bus2go.android.data.data_source.local.database.exo.entity.Routes
import app.bus2go.android.data.data_source.local.database.exo.entity.Shapes
import app.bus2go.android.data.data_source.local.database.exo.entity.StopTimes
import app.bus2go.android.data.data_source.local.database.exo.entity.Stops
import app.bus2go.android.data.data_source.local.database.exo.entity.Trips
import app.bus2go.android.data.data_source.local.Converters
import app.bus2go.android.domain.core.Bus2GoLocalDatabase


@Database(
    entities = [Routes::class, Trips::class, StopTimes::class, Stops::class, Calendar::class,
        CalendarDates::class, Shapes::class, Forms::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabaseExo : RoomDatabase(), Bus2GoLocalDatabase {
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
