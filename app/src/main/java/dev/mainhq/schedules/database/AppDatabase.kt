package dev.mainhq.schedules.database;

import android.content.Context
import androidx.room.Database;
import androidx.room.Room
import androidx.room.RoomDatabase;

import dev.mainhq.schedules.database.dao.RoutesDAO;
import dev.mainhq.schedules.database.dao.StopTimesDAO;
import dev.mainhq.schedules.database.dao.TripsDAO;
import dev.mainhq.schedules.database.entities.*;

@Database(entities = [Routes::class, Trips::class, StopTimes::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routesDao() : RoutesDAO;
    abstract fun tripsDao() : TripsDAO;
    abstract fun stopTimesDao() : StopTimesDAO;
}
