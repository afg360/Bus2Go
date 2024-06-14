package dev.mainhq.bus2go

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.adapters.TimeListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.icu.util.Calendar
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.RoomViewModel

//todo
//must be careful when dealing with hours AFTER 23:59:59
//since they may be considered in a new day in android, but not for stm
//todo add a home button to go back to the main activity
class Times : BaseActivity() {

    private var fromAlarmCreation = false
    private lateinit var agency: TransitAgency

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.times)
        val stopName = intent.getStringExtra("stopName")!!
        assert (stopName.isNotEmpty())
        agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra (AGENCY, TransitAgency::class.java) ?: throw AssertionError("AGENCY is Null")
        } else {
            intent.getSerializableExtra (AGENCY) as TransitAgency? ?: throw AssertionError("AGENCY is Null")
        }
        fromAlarmCreation = intent.getBooleanExtra("ALARMS", false)

        //val routeId = intent.extras?.getInt(ROUTE_ID)
        val roomViewModel = ViewModelProvider(this)[RoomViewModel::class.java]
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
        when(agency){
            TransitAgency.STM -> {
                val routeId = intent.extras!!.getInt(ROUTE_ID)
                val direction = intent.extras!!.getString(DIRECTION)!!
                //val directionId = intent.extras!!.getInt(DIRECTION_ID)
                lifecycleScope.launch {
                    val stopTimes = roomViewModel.getStopTimes(stopName, dayString, curTime.toString(), direction, agency, routeId)
                    withContext(Dispatchers.Main) {
                        //If stopTimes.isEmpty, say that it is empty
                        val recyclerView: RecyclerView = findViewById(R.id.time_recycle_view)
                        val layoutManager = LinearLayoutManager(applicationContext)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        recyclerView.layoutManager = layoutManager
                        //need to improve that code to make it more safe
                        recyclerView.adapter = TimeListElemsAdapter(stopTimes, fromAlarmCreation)
                    }
                }
            }
            TransitAgency.EXO_TRAIN -> {
                val routeId = intent.extras!!.getInt(ROUTE_ID)
                val directionId = intent.extras!!.getInt(DIRECTION_ID)
                lifecycleScope.launch {
                    val stopTimes = roomViewModel.getTrainStopTimes(routeId, stopName, directionId, curTime.toString(), dayString)
                    withContext(Dispatchers.Main) {
                        //If stopTimes.isEmpty, say that it is empty
                        val recyclerView: RecyclerView = findViewById(R.id.time_recycle_view)
                        val layoutManager = LinearLayoutManager(applicationContext)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        recyclerView.layoutManager = layoutManager
                        //need to improve that code to make it more safe
                        recyclerView.adapter = TimeListElemsAdapter(stopTimes, fromAlarmCreation)
                    }
                }
            }

            TransitAgency.EXO_OTHER -> {
                val routeId = intent.extras!!.getInt(ROUTE_ID)
                val headsign = intent.getStringExtra("headsign")!!
                lifecycleScope.launch {
                    val stopTimes = roomViewModel.getStopTimes(stopName, dayString, curTime.toString(), headsign, agency, routeId)
                    withContext(Dispatchers.Main) {
                        //If stopTimes.isEmpty, say that it is empty
                        val recyclerView: RecyclerView = findViewById(R.id.time_recycle_view)
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