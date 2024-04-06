package dev.mainhq.schedules.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import dev.mainhq.schedules.database.dao.*;
import dev.mainhq.schedules.database.entities.*;

@Database(entities = [Routes::class, Trips::class, StopTimes::class,
    Stops::class, Calendar::class, Shapes::class, Forms::class], version = 1)
//@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routesDao() : RoutesDAO;
    abstract fun tripsDao() : TripsDAO;
    abstract fun stopTimesDao() : StopTimesDAO;
    abstract fun stopDao() : StopsDAO
    abstract fun calendarDao() : CalendarDAO
    abstract fun shapesDao() : ShapesDAO
    abstract fun formsDao() : FormsDAO
}
