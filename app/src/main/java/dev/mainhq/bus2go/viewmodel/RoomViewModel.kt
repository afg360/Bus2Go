package dev.mainhq.bus2go.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dev.mainhq.bus2go.database.exo_data.AppDatabaseExo
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.fragments.FavouriteBusInfo
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.utils.BusAgency
import dev.mainhq.bus2go.utils.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.icu.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _stmDatabase : MutableStateFlow<AppDatabaseSTM?> = MutableStateFlow(
        Room.databaseBuilder(application, AppDatabaseSTM::class.java, "stm_info.db")
        .createFromAsset("database/stm_info.db").build())
    private val stmDatabase : StateFlow<AppDatabaseSTM?> get() = _stmDatabase

    private val _exoDatabase : MutableStateFlow<AppDatabaseExo?> = MutableStateFlow(
        Room.databaseBuilder(application, AppDatabaseExo::class.java, "exo_info.db")
            .createFromAsset("database/exo_info.db").build())
    private val exoDataBase : StateFlow<AppDatabaseExo?> get() = _exoDatabase

    suspend fun getStopTimes(list : List<BusInfo>, agency : BusAgency, dayString : String,
                     calendar : Calendar, times : MutableList<FavouriteBusInfo>) : MutableList<FavouriteBusInfo> {
        val stopsInfoDAO = stmDatabase.value?.stopsInfoDao()
        list.forEach {busInfo ->
            stopsInfoDAO?.getFavouriteStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.tripHeadsign)
                ?.also { time -> times.add(FavouriteBusInfo(busInfo, time, agency)) }
        }
        val stopTimesDAO = exoDataBase.value?.stopTimesDao()
        list.forEach {busInfo ->
            stopTimesDAO?.getFavouriteStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.tripHeadsign)
                ?.also { time -> times.add(FavouriteBusInfo(busInfo, time, agency)) }
        }
        return times
    }
}