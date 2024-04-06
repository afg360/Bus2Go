package dev.mainhq.schedules

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.database.dao.StopInfo
import dev.mainhq.schedules.utils.RecyclerViewItemListener
import dev.mainhq.schedules.utils.RecyclerViewItemListener.ClickListener
import dev.mainhq.schedules.utils.Time
import dev.mainhq.schedules.utils.adapters.TimeListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.Calendar

//todo
//instead of doing a huge query on getting the time, we could first retrieve
//all the possible stations (ordered by id, and prob based on localisation? -> not privacy friendly
//and once the user clicks, either new activity OR new fragment? -> in the latter case need to implement onback

class ChooseStop : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_stop)
        val busLine : String = intent.getStringExtra("BusLine").toString()
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "stm_info")
            .createFromAsset("database/stm_info.db").build()
        //verify speed
        lifecycleScope.launch {
            val stopTime = db.stopTimesDao()
            val cur = getTime()
            Log.d("CURRENT TIME", cur.toString())
            val stopName : List<StopInfo> = stopTime.getArrivalTimesFromBusNumNow(busLine)
            Log.d("QUERY", stopName.toString())
            displayTimes(stopName)
            //todo here to close or under the launch?
            db.close()
        }
    }

    private suspend fun displayTimes(list : List<StopInfo>){
        withContext(Dispatchers.Main){
            val recyclerView : RecyclerView = findViewById(R.id.time_recycle_view)
            val layoutManager = LinearLayoutManager(applicationContext)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = layoutManager
            recyclerView.addOnItemTouchListener(
                RecyclerViewItemListener(
                    applicationContext,
                    recyclerView,
                    object : ClickListener {
                        override fun onClick(view: View?, position: Int) {
                            //todo
                            //Toast.makeText(applicationContext, "Clicking works!", Toast.LENGTH_LONG).show()
                            val constraintLayout = view as ConstraintLayout
                            val stopCode = constraintLayout.tag as Int
                            Log.d("STOP CODE", stopCode.toString())
                            val intent = Intent(applicationContext, Times::class.java)
                            intent.putExtra("stopCode", stopCode)
                            startActivity(intent)
                        }

                        override fun onLongClick(view: View?, position: Int) {}
                    })
            )
            //need to improve that code to make it more safe
            //need to improve that code to make it more safe
            recyclerView.adapter = TimeListElemsAdapter(list)
        }
    }
    private fun getTime() : Time{
        val calendar : Calendar = Calendar.getInstance()
        return Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))
    }
}