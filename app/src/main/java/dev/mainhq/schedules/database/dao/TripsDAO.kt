package dev.mainhq.schedules.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.ArrayList;

import dev.mainhq.schedules.database.entities.Trips;

@Dao
interface TripsDAO {
    //trip column values are decale de 1
    @Query("SELECT DISTINCT trip_headsign FROM Trips WHERE tripid = (:tripid);")
    suspend fun getTripHeadsigns(tripid : Int) : List<String>;

    @Query("SELECT DISTINCT serviceid FROM Trips;")
    suspend fun getRouteId() : List<Int>;
    @Query("SELECT * FROM Trips WHERE id < 11;")
    suspend fun getAll() : List<Trips>;
    //todo this is tmp before fixing db
}
