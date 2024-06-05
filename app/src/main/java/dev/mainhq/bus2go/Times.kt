package dev.mainhq.bus2go

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.bus2go.database.stm_data.AppDatabase
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.utils.adapters.TimeListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.icu.util.Calendar

//todo
//must be careful when dealing with hours AFTER 23:59:59
//since they may be considered in a new day in android, but not for stm
//todo add a home button to go back to the main activity
class Times : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTheme()
        setContentView(R.layout.times)
        val stopName = intent.getStringExtra("stopName")!!
        assert (stopName.isNotEmpty())
        val headsign = intent.getStringExtra("headsign")!!
        lifecycleScope.launch {
            //check if connected to internet
            //if yes, make a web request
            //if not connected to internet, then below
            setup(stopName, headsign)
        }
    }

    private suspend fun setup(stopName : String, headsign : String){
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "stm_info.db")
            .createFromAsset("database/stm_info.db").build()
        val stopsInfo = db.stopsInfoDao()
        val calendar : Calendar = Calendar.getInstance()
        val curTime = Time(calendar)
        Log.d("CURRENT TIME", curTime.toString())
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
        val stopTimes = stopsInfo.getStopTimes(stopName, dayString, curTime.toString(), headsign)
        display(stopTimes)
    }

    private suspend fun display(list : List<Time>){
        withContext(Dispatchers.Main){
            val recyclerView : RecyclerView = findViewById(R.id.time_recycle_view)
            val layoutManager = LinearLayoutManager(applicationContext)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = layoutManager
            //need to improve that code to make it more safe
            recyclerView.adapter = TimeListElemsAdapter(list)
        }
    }
}