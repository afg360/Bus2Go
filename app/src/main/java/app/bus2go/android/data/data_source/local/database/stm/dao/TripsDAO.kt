package app.bus2go.android.data.data_source.local.database.stm.dao;

import androidx.room.Dao;
import androidx.room.Query;
import app.bus2go.android.domain.entity.stm.DirectionInfo

@Dao
interface TripsDAO {
    @Query("SELECT DISTINCT trip_headsign as tripHeadSign,direction_id as directionId " +
            "FROM Trips WHERE route_id = (:routeId) ORDER BY trip_headsign;")
    suspend fun getDirectionInfo(routeId : Int) : List<DirectionInfo.StmDirectionInfo>
}
