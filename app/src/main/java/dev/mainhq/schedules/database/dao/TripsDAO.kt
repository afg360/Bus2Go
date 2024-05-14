package dev.mainhq.schedules.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.ArrayList;

import dev.mainhq.schedules.database.entities.Trips;

@Dao
interface TripsDAO {
    @Query("SELECT DISTINCT trip_headsign FROM Trips WHERE route_id = (:routeId);")
    suspend fun getTripHeadsigns(routeId : Int) : List<String>;

    @Query("SELECT DISTINCT route_id FROM Trips;")
    suspend fun getRouteId() : List<Int>;
}
