package dev.mainhq.bus2go.database.exo_data.dao;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
interface TripsDAO {
    @Query("SELECT DISTINCT trip_headsign FROM Trips WHERE route_id = (:routeId);")
    suspend fun getTripHeadsigns(routeId : Int) : List<String>;

    @Query("SELECT DISTINCT route_id FROM Trips;")
    suspend fun getRouteId() : List<String>;
}
