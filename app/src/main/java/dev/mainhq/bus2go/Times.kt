package dev.mainhq.bus2go

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.adapters.TimeListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.icu.util.Calendar
import android.os.Build
import dev.mainhq.bus2go.database.exo_data.AppDatabaseExo
import dev.mainhq.bus2go.utils.BusAgency
import kotlinx.coroutines.async
import kotlin.properties.Delegates

//todo
//must be careful when dealing with hours AFTER 23:59:59
//since they may be considered in a new day in android, but not for stm
//todo add a home button to go back to the main activity
class Times : BaseActivity() {

    private var fromAlarmCreation by Delegates.notNull<Boolean>()
    private lateinit var agency: BusAgency

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTheme()
        setContentView(R.layout.times)
        val stopName = intent.getStringExtra("stopName")!!
        assert (stopName.isNotEmpty())
        val headsign = intent.getStringExtra("headsign")!!
        agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra (AGENCY, BusAgency::class.java) ?: throw AssertionError("AGENCY is Null")
        } else {
            intent.getSerializableExtra (AGENCY) as BusAgency? ?: throw AssertionError("AGENCY is Null")
        }
        fromAlarmCreation = intent.getBooleanExtra("ALARMS", false)
        when(agency){
            BusAgency.STM -> {
                lifecycleScope.launch {
                    //check if connected to internet
                    //if yes, make a web request
                    //if not connected to internet, then below
                    val job = async {
                        val db = Room.databaseBuilder(applicationContext, AppDatabaseSTM::class.java, "stm_info.db")
                            .createFromAsset("database/stm_info.db").build()
                        db.stopsInfoDao()
                    }

                    val calendar : Calendar = Calendar.getInstance()
                    val dayString = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "d"
                        Calendar.MONDAY -> "m"
                        Calendar.TUESDAY -> "t"
                        Calendar.WEDNESDAY -> "w"
                        Calendar.THURSDAY -> "y"
                        Calendar.FRIDAY -> "f"
                        Calendar.SATURDAY -> "s"
                        else -> null
                    }
                    dayString ?: throw IllegalStateException("Cannot have a non day of the week!")
                    val stopTimes = if (fromAlarmCreation){
                        job.await().getStopTimes(stopName, dayString, headsign)
                    } else{
                        val curTime = Time(calendar)
                        Log.d("CURRENT TIME", curTime.toString())
                        job.await().getStopTimes(stopName, dayString, curTime.toString(), headsign)
                    }
                    withContext(Dispatchers.Main){
                        val recyclerView : RecyclerView = findViewById(R.id.time_recycle_view)
                        val layoutManager = LinearLayoutManager(applicationContext)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        recyclerView.layoutManager = layoutManager
                        //need to improve that code to make it more safe
                        recyclerView.adapter = TimeListElemsAdapter(stopTimes, fromAlarmCreation)
                    }
                }
            }
            BusAgency.EXO -> {
                lifecycleScope.launch {
                    val job = async {
                        val db = Room.databaseBuilder(applicationContext, AppDatabaseExo::class.java, "exo_info.db")
                            .createFromAsset("database/exo_info.db").build()
                        db.stopTimesDao()
                    }
                    val calendar : Calendar = Calendar.getInstance()
                    val dayString = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "d"
                        Calendar.MONDAY -> "m"
                        Calendar.TUESDAY -> "t"
                        Calendar.WEDNESDAY -> "w"
                        Calendar.THURSDAY -> "y"
                        Calendar.FRIDAY -> "f"
                        Calendar.SATURDAY -> "s"
                        else -> null
                    }
                    dayString ?: throw IllegalStateException("Cannot have a non day of the week!")
                    val curTime = Time(calendar)
                    Log.d("CURRENT TIME", curTime.toString())
                    val stopTimes = job.await().getStopTimes(stopName, dayString, curTime.toString(), headsign)
                    withContext(Dispatchers.Main){
                        val recyclerView : RecyclerView = findViewById(R.id.time_recycle_view)
                        val layoutManager = LinearLayoutManager(applicationContext)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        recyclerView.layoutManager = layoutManager
                        //need to improve that code to make it more safe
                        recyclerView.adapter = TimeListElemsAdapter(stopTimes, fromAlarmCreation)
                    }
                }
            }
        }

    }
}