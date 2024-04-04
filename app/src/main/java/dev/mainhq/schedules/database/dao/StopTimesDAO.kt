package dev.mainhq.schedules.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import dev.mainhq.schedules.database.entities.StopTimes;

@Dao
interface StopTimesDAO {
    //todo the query trips.serviceid is very wrong!!! Need to update database
    @Query("SELECT arrivaltime FROM StopTimes JOIN Trips ON StopTimes.tripid = Trips.tripid " +
            "WHERE Trips.trip_headsign LIKE (:headsign);")
    suspend fun getArrivalTimes(headsign : String) : List<String>;
}
