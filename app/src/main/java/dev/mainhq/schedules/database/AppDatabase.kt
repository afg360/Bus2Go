package dev.mainhq.schedules.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters

import dev.mainhq.schedules.database.dao.*;
import dev.mainhq.schedules.database.entities.*;

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
