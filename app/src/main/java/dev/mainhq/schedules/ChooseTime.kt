package dev.mainhq.schedules

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import dev.mainhq.schedules.database.AppDatabase
import kotlinx.coroutines.launch

class ChooseTime : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_time)
        val busLine : String = intent.getStringExtra("BusLine").toString()
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "stm_info")
            .createFromAsset("database/stm_info.db").build()

        lifecycleScope.launch {
            val stopTimes = db.stopTimesDao()
            val arrivalTimes = stopTimes.getArrivalTimes(busLine)//busLine)
            if (arrivalTimes.isNotEmpty())
                Log.d("ARRIVALS", arrivalTimes.toString())
            else
                Log.e("ARRIVALS", "ERROR IN DB")
        }
    }
}