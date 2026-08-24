package dev.mainhq.bus2go.data.data_source.local.database.stm;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.mainhq.bus2go.data.data_source.local.Converters
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.CalendarDatesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.FeedInfoDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.RoutesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.ShapesDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.StopsInfoDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.dao.TripsDAO
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Calendar
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.CalendarDates
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.FeedInfo
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Routes
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Shapes
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Stops
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.StopsInfo
import dev.mainhq.bus2go.data.data_source.local.database.stm.entity.Trips
import java.sql.SQLException

@Database(
    entities = [Routes::class, Trips::class, StopsInfo::class,
        Stops::class, Calendar::class, CalendarDates::class, Shapes::class, FeedInfo::class],
    version = 3,
    exportSchema = true
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
    abstract fun feedInfoDao() : FeedInfoDAO
//    abstract fun configDao(): ConfigDAO

    companion object {
        //TODO eventually write the version name on filename, not in config file
        const val FILENAME_PREFIX = "stm_data"
        const val DATABASE_NAME = "$FILENAME_PREFIX.db"
        const val DATABASE_PATH = "databases/$DATABASE_NAME"

        //we will add a calendar_dates table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            @Throws(SQLException::class)
            override fun migrate(db: SupportSQLiteDatabase) {
                //changes:
                //  added a table CalendarDates
                //  got rid of ID in calendar, primary key is now service_id
                //  must change references to calendar?
                //need to recreate table Calendar, by copying, dropping old, and renaming new
                db.beginTransaction()
                db.execSQL("CREATE TABLE IF NOT EXISTS Copy(" +
                    "service_id TEXT PRIMARY KEY NOT NULL," +
                    "days TEXT NOT NULL," +
                    "start_date INTEGER NOT NULL," +
                    "end_date INTEGER NOT NULL)"
                )
                db.execSQL("INSERT INTO Copy (service_id, days, start_date, end_date) " +
                    "SELECT service_id, days, start_date, end_date FROM Calendar"
                )
                db.execSQL("DROP TABLE Calendar")
                db.execSQL("ALTER TABLE Copy RENAME to Calendar")

                db.execSQL("CREATE TABLE IF NOT EXISTS CalendarDates (" +
                    "service_id TEXT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "exception_type INTEGER NOT NULL, " +
                    "PRIMARY KEY (service_id, date), " +
                    "FOREIGN KEY(service_id) REFERENCES Calendar(service_id))"
                )
                db.endTransaction()
            }
        }

        val MIGRATION_2_3 = object: Migration(2, 3) {
            @Throws(SQLException::class)
            override fun migrate(db: SupportSQLiteDatabase) {
                //changes: changed trip_id type form INTEGER to TEXT
                db.beginTransaction()
                db.execSQL("""CREATE TABLE TripsNew(
                    id INTEGER PRIMARY KEY NOT NULL,
                    trip_id TEXT NOT NULL,
                    route_id INTEGER NOT NULL REFERENCES Routes(route_id),
                    service_id TEXT NOT NULL REFERENCES Calendar(service_id),
                    trip_headsign TEXT NOT NULL,
                    direction_id INTEGER NOT NULL,
                    shape_id INTEGER NOT NULL REFERENCES "Forms"(shape_id),
                    wheelchair_accessible INTEGER NOT NULL
                )""")
                db.execSQL("""
                    INSERT INTO TripsNew(id, trip_id, route_id, service_id, trip_headsign, direction_id, shape_id, wheelchair_accessible)
                    SELECT Trips.id, CAST(Trips.trip_id AS TEXT), Trips.route_id, Trips.service_id, Trips.trip_headsign, Trips.direction_id, Trips.shape_id, Trips.wheelchair_accessible
                    FROM Trips
                """)
                db.execSQL("""DROP TABLE Trips""")
                db.execSQL("""ALTER TABLE TripsNew RENAME TO Trips""")
                db.endTransaction()
            }
        }
    }
}
