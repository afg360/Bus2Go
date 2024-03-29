package dev.mainhq.schedules.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import dev.mainhq.schedules.database.dao.RoutesDAO;
import dev.mainhq.schedules.database.dao.StopTimesDAO;
import dev.mainhq.schedules.database.dao.TripsDAO;
import dev.mainhq.schedules.database.entities.*;

@Database(entities = {Routes.class, Trips.class, StopTimes.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RoutesDAO routesDao();
    public abstract TripsDAO tripsDao();
    public abstract StopTimesDAO stopTimesDao();
}
