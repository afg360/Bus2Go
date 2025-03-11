package dev.mainhq.bus2go.data.data_source.local.database.stm.dao;

import dev.mainhq.bus2go.domain.entity.BusRouteInfo
import androidx.room.Dao;
import androidx.room.Query;
import dev.mainhq.bus2go.utils.FuzzyQuery

@Dao
interface RoutesDAO {

    @Query("SELECT route_long_name FROM Routes;")
    suspend fun getBusDir() : List<String>;

    //useless???
    @Query("SELECT DISTINCT route_color FROM Routes WHERE route_id = (:routeId);")
    suspend fun getRouteColor(routeId : Int) : String;

    @Query("SELECT route_id AS routeId,route_long_name AS routeName FROM Routes " +
            "WHERE CAST(route_id AS TEXT) LIKE '%' || (:routeId) || '%' " +
            "OR route_long_name LIKE '%' || (:routeId) || '%' ;")
    suspend fun getBusRouteInfo(routeId : FuzzyQuery) : List<BusRouteInfo>

}
