package dev.mainhq.bus2go.database.stm_data.dao;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
interface TripsDAO {
    @Query("SELECT DISTINCT trip_headsign as tripHeadSign,direction_id as directionId " +
            "FROM Trips WHERE route_id = (:routeId) ORDER BY trip_headsign;")
    suspend fun getDirectionInfo(routeId : Int) : List<DirectionInfo>;

    @Query("SELECT DISTINCT route_id FROM Trips;")
    suspend fun getRouteId() : List<Int>;
}

data class DirectionInfo(val tripHeadSign : String, val directionId : Int)