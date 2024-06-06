package dev.mainhq.bus2go.database.stm_data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters
import dev.mainhq.bus2go.database.Converters
import dev.mainhq.bus2go.database.stm_data.dao.CalendarDAO
import dev.mainhq.bus2go.database.stm_data.dao.FormsDAO
import dev.mainhq.bus2go.database.stm_data.dao.RoutesDAO
import dev.mainhq.bus2go.database.stm_data.dao.ShapesDAO
import dev.mainhq.bus2go.database.stm_data.dao.StopsDAO
import dev.mainhq.bus2go.database.stm_data.dao.StopsInfoDAO
import dev.mainhq.bus2go.database.stm_data.dao.TripsDAO
import dev.mainhq.bus2go.database.stm_data.entities.Calendar
import dev.mainhq.bus2go.database.stm_data.entities.Forms
import dev.mainhq.bus2go.database.stm_data.entities.Routes
import dev.mainhq.bus2go.database.stm_data.entities.Shapes
import dev.mainhq.bus2go.database.stm_data.entities.Stops
import dev.mainhq.bus2go.database.stm_data.entities.StopsInfo
import dev.mainhq.bus2go.database.stm_data.entities.Trips

@Database(entities = [Routes::class, Trips::class, StopsInfo::class,
    Stops::class, Calendar::class, Shapes::class, Forms::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabaseSTM : RoomDatabase() {
    abstract fun routesDao() : RoutesDAO
    abstract fun tripsDao() : TripsDAO
    abstract fun stopsInfoDao() : StopsInfoDAO
    abstract fun stopDao() : StopsDAO
    abstract fun calendarDao() : CalendarDAO
    abstract fun shapesDao() : ShapesDAO
    abstract fun formsDao() : FormsDAO
}
