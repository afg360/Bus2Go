package dev.mainhq.bus2go

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.adapters.TimeListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Build
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.Job
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

//todo
//must be careful when dealing with hours AFTER 23:59:59
//since they may be considered in a new day in android, but not for stm
//todo add a home button to go back to the main activity
class TimesActivity : BaseActivity() {

    private var fromAlarmCreation = false

    private var executor : ScheduledExecutorService? = null
    private var scheduledTask: ScheduledFuture<*>? = null
    //may perhaps be not required?
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.times_activity)
        val stopName = intent.getStringExtra("stopName")!!
        assert (stopName.isNotEmpty())
        val agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra (BusExtrasInfo.AGENCY.name, TransitAgency::class.java) ?: throw AssertionError("AGENCY is Null")
        } else {
            intent.getSerializableExtra (BusExtrasInfo.AGENCY.name) as TransitAgency? ?: throw AssertionError("AGENCY is Null")
        }
        fromAlarmCreation = intent.getBooleanExtra("ALARMS", false)

        executor = Executors.newSingleThreadScheduledExecutor()
        //val routeId = intent.extras?.getInt(BusExtrasInfo.ROUTE_ID.name)
        val roomViewModel = ViewModelProvider(this)[RoomViewModel::class.java]
        //val calendar : Calendar = Calendar.getInstance()
        val time = Time.now()
        val textView = findViewById<MaterialTextView>(R.id.time_title_text_view)
        //FIXME since TransitData is parcelable, pass it around instead (and deal with Agencies to do correct Type Conversions
        when(agency){
            TransitAgency.STM -> {
                //FIXME this may fail in the case the route id is a metro or other stuff?
                val routeId = intent.extras!!.getString(BusExtrasInfo.ROUTE_ID.name)!!.toInt()
                val direction = intent.extras!!.getString(BusExtrasInfo.DIRECTION.name)!!
                //val directionId = intent.extras!!.getInt(BusExtrasInfo.DIRECTION.name_ID)
                textView.text = "$routeId $stopName -> $direction"
                lifecycleScope.launch {
                    val stopTimes = roomViewModel.getStmStopTimes(stopName, time, direction, routeId)
                    displayRecyclerView(stopTimes)
                    setupScheduledTask(stopTimes)
                }
            }
            TransitAgency.EXO_TRAIN -> {
                val routeId = intent.extras!!.getString(BusExtrasInfo.ROUTE_ID.name)!!.toInt()
                val directionId = intent.extras!!.getInt(BusExtrasInfo.DIRECTION_ID.name)
                val trainNum = intent.extras!!.getInt(BusExtrasInfo.TRAIN_NUM.name)
                val direction = intent.extras!!.getString(BusExtrasInfo.DIRECTION.name)
                textView.text = "#$trainNum $stopName -> $direction"
                lifecycleScope.launch {
                    val stopTimes = roomViewModel.getTrainStopTimes(routeId, stopName, directionId, time)
                    displayRecyclerView(stopTimes)
                    setupScheduledTask(stopTimes)
                }
            }

            TransitAgency.EXO_OTHER -> {
                //FIXME routeId may not always be an int?
                val routeId = intent.extras!!.getString(BusExtrasInfo.ROUTE_ID.name)!!
                val headsign = intent.getStringExtra(BusExtrasInfo.HEADSIGN.name)!!
                textView.text = "$routeId $stopName -> $headsign"
                lifecycleScope.launch {
                    val stopTimes = roomViewModel.getExoOtherStopTimes(stopName, time, headsign)
                    displayRecyclerView(stopTimes)
                    setupScheduledTask(stopTimes)
                }
            }
        }
    }

    //or perhaps it is onStop we need to override instead?
    override fun onDestroy() {
        super.onDestroy()

        job?.cancel()
        scheduledTask?.cancel(true)
        executor?.shutdown()

    }

    //FIXME: Although this implementation works, we need to get rid of the recyclerViewItem once we go beyond
    //FIXME may need a LocalDateTime
    //the time... unless that is already dealt with?
    private fun setupScheduledTask(stopTimes: List<Time>){
        scheduledTask = executor?.scheduleWithFixedDelay({
            job = lifecycleScope.launch{
                displayRecyclerView(stopTimes)
            }
        }, 0, 20, TimeUnit.SECONDS)

    }

    //FIXME may need a fucking localdatetime instead of a localtime
    private suspend fun displayRecyclerView(stopTimes: List<Time>){
        withContext(Dispatchers.Main) {
            //If stopTimes.isEmpty, say that it is empty
            val recyclerView: RecyclerView = findViewById(R.id.time_recycle_view)
            if (stopTimes.isEmpty()){
                recyclerView.visibility = View.GONE
                val textView = findViewById<MaterialTextView>(R.id.no_available_transit_left_text_view)
                textView.visibility = View.VISIBLE
                textView.text
            }
            else{
                val layoutManager = LinearLayoutManager(applicationContext)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                recyclerView.layoutManager = layoutManager
                //need to improve that code to make it more safe
                recyclerView.adapter = TimeListElemsAdapter(stopTimes, fromAlarmCreation)
            }
        }
    }
}