package dev.mainhq.bus2go.data.data_source.local.database.exo.dao;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
interface TripsDAO {
    @Query("SELECT DISTINCT trip_headsign FROM Trips WHERE route_id LIKE '%' || (:routeId);")
    suspend fun getTripHeadsigns(routeId : String) : List<String>

    @Query("SELECT DISTINCT trip_headsign FROM Trips WHERE route_id = (:routeId) " +
            "AND direction_id = (:directionId);")
    suspend fun getTrainTripHeadsigns(routeId: Int, directionId : Int) : List<String>

    @Query("SELECT DISTINCT route_id FROM Trips;")
    suspend fun getRouteId() : List<String>

    /** Used to migrate from favourites.json v1 to v2 */
    @Query("SELECT DISTINCT route_id FROM Trips WHERE trip_headsign = (:headsign)")
    suspend fun getRouteId(headsign: String): String
}
