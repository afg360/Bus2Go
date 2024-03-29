package dev.mainhq.schedules.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
interface RoutesDAO {

    @Query("SELECT route_long_name FROM Routes;")
    suspend fun getBusDir() : List<String>;

    @Query("SELECT DISTINCT route_color FROM Routes WHERE routeId = (:routeId);")
    suspend fun getRouteColor(routeId : Int) : String;

}
