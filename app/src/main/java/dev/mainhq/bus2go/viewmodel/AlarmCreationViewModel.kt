package dev.mainhq.bus2go.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.fragments.AlarmCreationChooseBusDialog.AlarmBusInfo
import dev.mainhq.bus2go.preferences.Alarm
import dev.mainhq.bus2go.preferences.AlarmsData
import dev.mainhq.bus2go.preferences.AlarmsSerializer
import dev.mainhq.bus2go.preferences.SerializableTime
import dev.mainhq.bus2go.utils.Time
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/** Used to save the state when creating a new alarm. */
class AlarmCreationViewModel(private val application : Application) : AndroidViewModel(application) {

    private val _alarmBusInfo : MutableLiveData<AlarmBusInfo> = MutableLiveData()
        val alarmBusInfo : LiveData<AlarmBusInfo> get() = _alarmBusInfo

    private val _chosenDays : MutableLiveData<String> = MutableLiveData("hello")
        val chosenDays: LiveData<String> get() = _chosenDays

    private val Context.alarmDataStore : DataStore<AlarmsData> by dataStore(
        fileName = "alarms.json",
        serializer = AlarmsSerializer
    )

    fun updateAlarmBus(alarmBusInfo: AlarmBusInfo){
        _alarmBusInfo.value = alarmBusInfo
    }

    fun updateDays(dateString: String){
        _chosenDays.value = dateString //TODO TO PARSE THE STRING/LIST
    }

    fun createAlarm(){
        //TODO check if good data before storing
        viewModelScope.launch {
            application.alarmDataStore.updateData {alarmsData ->
                alarmsData.copy(list = alarmsData.list.mutate {
                    it.add(Alarm(
                        "Hello World",
                        alarmBusInfo.value!!.busInfo,
                        alarmBusInfo.value!!.time.run { SerializableTime(hour, min, sec) },
                        chosenDays.value!!
                    ))
                })
            }
        }
    }

    fun createAlarm(block : (() -> Unit)){
        //TODO check if good data before storing
        viewModelScope.launch {
            application.alarmDataStore.updateData {alarmsData ->
                alarmsData.copy(list = alarmsData.list.mutate {
                    it.add(Alarm(
                        "Hello World",
                        alarmBusInfo.value!!.busInfo,
                        alarmBusInfo.value!!.time.run { SerializableTime(hour, min, sec) },
                        chosenDays.value!!
                    ))
                })
            }
            block()
        }
    }

    suspend fun readAlarms() : List<Alarm>{
        return application.alarmDataStore.data.first().list.toList()
    }
}