package dev.mainhq.bus2go.data.data_source.local.database.stm.dao;

import androidx.room.Dao;
import androidx.room.Query;
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo

@Dao
interface TripsDAO {
    @Query("SELECT DISTINCT trip_headsign as tripHeadSign,direction_id as directionId " +
            "FROM Trips WHERE route_id = (:routeId) ORDER BY trip_headsign;")
    suspend fun getDirectionInfo(routeId : Int) : List<DirectionInfo.StmDirectionInfo>

    //@Query("SELECT DISTINCT route_id FROM Trips;")
    //suspend fun getRouteId() : List<Int>
}
