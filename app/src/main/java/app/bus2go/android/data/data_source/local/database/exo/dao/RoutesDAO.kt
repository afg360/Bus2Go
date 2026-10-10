package app.bus2go.android.data.data_source.local.database.exo.dao;

import androidx.room.Dao;
import androidx.room.Query;
import app.bus2go.android.data.data_source.local.database.RouteInfoDto
import app.bus2go.android.domain.entity.FuzzyQuery

@Dao
interface RoutesDAO {

    @Query("SELECT route_long_name FROM Routes;")
    suspend fun getBusDir() : List<String>

    @Query("SELECT DISTINCT route_long_name FROM Routes WHERE route_id = (:routeId);")
    suspend fun getBusDir(routeId: String) : String

    //@Query("SELECT DISTINCT route_color AS routeColor,route_text_color AS routeTextColor FROM Routes WHERE route_id = (:routeId);")
    //suspend fun getRouteColor(routeId : String) : Colors

    @Query("SELECT route_id AS routeId,route_long_name AS routeName FROM Routes " +
            "WHERE route_id LIKE '%' || (:routeId) || '%' " +
            "OR route_long_name LIKE '%' || (:routeId) || '%' ;")
    suspend fun getRouteInfo(routeId : FuzzyQuery) : List<RouteInfoDto>

    @Query("SELECT route_id as routeId FROM Routes " +
            "WHERE route_id LIKE 'trains-' || (:routeId);")
    suspend fun getTrainRouteAgency(routeId: String) : List<String>

}

//data class Colors(val routeColor : String, val routeTextColor : String)