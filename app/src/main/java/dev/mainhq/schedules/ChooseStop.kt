package dev.mainhq.schedules

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.utils.adapters.StopListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//todo
//instead of doing a huge query on getting the time, we could first retrieve
//all the possible stations (ordered by id, and prob based on localisation? -> not privacy friendly
//and once the user clicks, either new activity OR new fragment? -> in the latter case need to implement onback
//todo add possibility of searching amongst all the stops
class ChooseStop : AppCompatActivity() {
    private lateinit var headsign : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_stop)
        headsign = intent.getStringExtra("headsign").toString()

        lifecycleScope.launch {
            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "stm_info")
                .createFromAsset("database/stm_info.db").build()
            val stopInfo = db.stopsInfoDao()
            val stopNames = stopInfo.getStopNames(headsign)
            Log.d("STOP NAMES", stopNames.toString())
            displayTimes(stopNames)
            //todo here to close or under the launch?
        }
    }

    private suspend fun displayTimes(list : List<String>){
        withContext(Dispatchers.Main){
            if (list.isNotEmpty()) {
                val recyclerView: RecyclerView = findViewById(R.id.stop_recycle_view)
                val layoutManager = LinearLayoutManager(applicationContext)
                recyclerView.adapter = StopListElemsAdapter(list, headsign)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                recyclerView.layoutManager = layoutManager
            }
            else{
                TODO("Display message saying no stops found")
            }
        }
    }
}