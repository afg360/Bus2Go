package dev.mainhq.bus2go.database.exo_data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters
import dev.mainhq.bus2go.database.Converters
import dev.mainhq.bus2go.database.exo_data.dao.*
import dev.mainhq.bus2go.database.exo_data.entities.*

@Database(entities = [Routes::class, Trips::class, StopTimes::class,
    Stops::class, Calendar::class, Shapes::class, Forms::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabaseExo : RoomDatabase() {
    abstract fun routesDao() : RoutesDAO
    abstract fun tripsDao() : TripsDAO
    abstract fun stopDao() : StopsDAO
    abstract fun stopTimesDao() : StopTimesDAO
    abstract fun calendarDao() : CalendarDAO
    abstract fun shapesDao() : ShapesDAO
    abstract fun formsDao() : FormsDAO
}
