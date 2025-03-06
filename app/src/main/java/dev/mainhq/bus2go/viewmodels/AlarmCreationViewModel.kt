package dev.mainhq.bus2go.viewmodels

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

private val Context.alarmDataStore : DataStore<AlarmsData> by dataStore(
    fileName = "alarms.json",
    serializer = AlarmsSerializer
)

//FIXME UPDATE THIS VIEW MODEL TO STORE BETTER ORGANISED DATA
/** Used to save the state when creating a new alarm. */
class AlarmCreationViewModel(private val application : Application) : AndroidViewModel(application) {

    private val _alarmBusInfo : MutableLiveData<AlarmBusInfo> = MutableLiveData()
        val alarmBusInfo : LiveData<AlarmBusInfo> get() = _alarmBusInfo

    private val _isOn : MutableLiveData<Boolean> = MutableLiveData(true)
        val isOn : LiveData<Boolean> get() = _isOn

    private val _chosenDays : MutableLiveData<Map<Char, Boolean>> = MutableLiveData(mapOf(
        Pair('d', false), Pair('m', false), Pair('t', false), Pair('w', false), Pair('y', false), Pair('f', false), Pair('s', false)
    ))
        val chosenDays: LiveData<Map<Char, Boolean>> get() = _chosenDays

    private val _beforeTime : MutableLiveData<SerializableTime> = MutableLiveData()
    val beforeTime : LiveData<SerializableTime> get() = _beforeTime

    fun updateAlarmBusInfo(alarmBusInfo: AlarmBusInfo){
        _alarmBusInfo.value = alarmBusInfo
    }

    fun updateDays(daysOn: Map<Char, Boolean>){
        //_chosenDays.value = daysOn //TODO TO PARSE THE STRING/LIST
    }

    fun updateBeforeTime(time : Time){
        //_beforeTime.value = time.toSerializableTime()
    }

    fun updateLiveActivatedState(id : Int, state : Boolean){
        _isOn.value = state
        viewModelScope.launch {
            application.alarmDataStore.updateData {alarmsData ->
                alarmsData.copy(list = alarmsData.list.mutate {list ->
                    /*
                    list[id - 1] = Alarm(
                        list[id - 1].id,
                        list[id - 1].title,
                        list[id - 1].busInfo,
                        list[id - 1].timeBefore,
                        list[id - 1].ringDays,
                        state,
                    )
                     */
                })
            }
        }
    }

    fun createAlarm(){
        viewModelScope.launch {
            application.alarmDataStore.updateData {alarmsData ->
                alarmsData.copy(list = alarmsData.list.mutate {
                    /*
                    it.add(Alarm(
                        //FIXME THIS PART WILL FAIL UNIQUENESS WHEN DELETING AND CREATING NEW ALARMS (and keeping older ones)
                        id = application.alarmDataStore.data.first().list.size + 1,
                        title = "Hello World",
                        alarmBusInfo.value!!.busInfo,
                        alarmBusInfo.value!!.time.run { SerializableTime(hour, min, sec) },
                        chosenDays.value!!,
                        isOn.value!!
                    ))
                    */
                })
            }
        }
    }

    fun createAlarm(/** Task to be done after the alarm is added to the list of alarms */
                    block : (() -> Unit)){
        //TODO check if good data before storing
        viewModelScope.launch {
            application.alarmDataStore.updateData {alarmsData ->
                alarmsData.copy(list = alarmsData.list.mutate {
                    /*
                    it.add(Alarm(
                        id = application.alarmDataStore.data.first().list.size + 1,
                        title = "Hello World",
                        alarmBusInfo.value!!.busInfo,
                        alarmBusInfo.value!!.time.run { SerializableTime(hour, min, sec) },
                        chosenDays.value!!,
                        isOn.value!!
                    ))
                    */
                })
            }
            block()
        }
    }

    /** Used to update the alarm stored inside the datastore, NOT
     *  the current information for the selected alarm */
    fun updateAlarm(id : Int){
        viewModelScope.launch {
            application.alarmDataStore.updateData {alarmsData ->
                alarmsData.copy(list = alarmsData.list.mutate {
                    //FIXME need a better way to refer to the correct alarm
                    it.removeAt(id - 1)
                    /*
                    it.add(Alarm(
                        id = id,
                        title = "Hello World",
                        alarmBusInfo.value!!.busInfo,
                        alarmBusInfo.value!!.time.run { SerializableTime(hour, min, sec) },
                        chosenDays.value!!,
                        isOn.value!!
                    ))
                     */
                })
            }
        }
    }


    suspend fun readAlarms() : List<Alarm>{
        return application.alarmDataStore.data.first().list.toList()
    }
}