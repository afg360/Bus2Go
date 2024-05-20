package dev.mainhq.bus2go.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters
import dev.mainhq.bus2go.database.dao.CalendarDAO
import dev.mainhq.bus2go.database.dao.FormsDAO
import dev.mainhq.bus2go.database.dao.RoutesDAO
import dev.mainhq.bus2go.database.dao.ShapesDAO
import dev.mainhq.bus2go.database.dao.StopsDAO
import dev.mainhq.bus2go.database.dao.StopsInfoDAO
import dev.mainhq.bus2go.database.dao.TripsDAO
import dev.mainhq.bus2go.database.entities.Calendar
import dev.mainhq.bus2go.database.entities.Forms
import dev.mainhq.bus2go.database.entities.Routes
import dev.mainhq.bus2go.database.entities.Shapes
import dev.mainhq.bus2go.database.entities.Stops
import dev.mainhq.bus2go.database.entities.StopsInfo
import dev.mainhq.bus2go.database.entities.Trips

@Database(entities = [Routes::class, Trips::class, StopsInfo::class,
    Stops::class, Calendar::class, Shapes::class, Forms::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routesDao() : RoutesDAO;
    abstract fun tripsDao() : TripsDAO;
    abstract fun stopsInfoDao() : StopsInfoDAO;
    abstract fun stopDao() : StopsDAO
    abstract fun calendarDao() : CalendarDAO
    abstract fun shapesDao() : ShapesDAO
    abstract fun formsDao() : FormsDAO
}
