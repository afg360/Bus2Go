package dev.mainhq.schedules

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.media3.test.utils.Action.Stop
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.utils.RecyclerViewItemListener
import dev.mainhq.schedules.utils.RecyclerViewItemListener.ClickListener
import dev.mainhq.schedules.utils.adapters.StopListElemsAdapter
import dev.mainhq.schedules.utils.adapters.TimeListElemsAdapter
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
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                recyclerView.layoutManager = layoutManager
                recyclerView.addOnItemTouchListener(
                    RecyclerViewItemListener(
                        applicationContext, recyclerView,
                        object : ClickListener {
                            override fun onClick(view: View?, position: Int) {
                                val textView: TextView =
                                    (view as ConstraintLayout).getChildAt(0)!! as TextView
                                val stopName = textView.text as String
                                Log.d("STOP NAME", stopName)
                                val intent = Intent(applicationContext, Times::class.java)
                                intent.putExtra("stopName", stopName)
                                intent.putExtra("headsign", headsign)
                                startActivity(intent)
                            }

                            override fun onLongClick(view: View?, position: Int) {}
                        })
                )
                //need to improve that code to make it more safe
                recyclerView.adapter = StopListElemsAdapter(list)
            }
            else{
                TODO("Display message saying no stops found")
            }
        }
    }
}