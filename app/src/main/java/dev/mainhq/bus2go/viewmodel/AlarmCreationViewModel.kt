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
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/** Used to save the state when creating a new alarm. */
class AlarmCreationViewModel(private val application : Application) : AndroidViewModel(application) {

    private val _alarmBusInfo : MutableLiveData<AlarmBusInfo> = MutableLiveData()
        val alarmBusInfo : LiveData<AlarmBusInfo> get() = _alarmBusInfo

    private val _isOn : MutableLiveData<Boolean> = MutableLiveData(true)
        val isOn : LiveData<Boolean> get() = _isOn

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

    fun updateLiveActivatedState(id : Int, state : Boolean){
        _isOn.value = state
        viewModelScope.launch {
            application.alarmDataStore.updateData {alarmsData ->
                alarmsData.copy(list = alarmsData.list.mutate {list ->
                    list[id - 1] = Alarm(
                        list[id - 1].id,
                        list[id - 1].title,
                        list[id - 1].busInfo,
                        list[id - 1].timeBefore,
                        list[id - 1].ringDays,
                        !list[id - 1].isOn,
                    )
                })
            }
        }
    }

    fun createAlarm(){
        viewModelScope.launch {
            application.alarmDataStore.updateData {alarmsData ->
                alarmsData.copy(list = alarmsData.list.mutate {
                    it.add(Alarm(
                        //FIXME THIS PART WILL FAIL UNIQUENESS WHEN DELETING AND CREATING NEW ALARMS (and keeping older ones)
                        id = application.alarmDataStore.data.first().list.size + 1,
                        title = "Hello World",
                        alarmBusInfo.value!!.busInfo,
                        alarmBusInfo.value!!.time.run { SerializableTime(hour, min, sec) },
                        chosenDays.value!!,
                        isOn.value!!
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
                        id = application.alarmDataStore.data.first().list.size + 1,
                        title = "Hello World",
                        alarmBusInfo.value!!.busInfo,
                        alarmBusInfo.value!!.time.run { SerializableTime(hour, min, sec) },
                        chosenDays.value!!,
                        isOn.value!!
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