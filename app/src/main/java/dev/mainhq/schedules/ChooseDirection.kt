package dev.mainhq.schedules;

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.utils.adapters.StopListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

//todo
//change appbar to be only a back button
//todo may make it a swapable ui instead of choosing button0 or 1
class ChooseDirection : AppCompatActivity() {
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState);
        val extras : Bundle = this.intent.extras ?: throw AssertionError("Assertion failed");
        val busName = extras.getString("busName") ?: throw AssertionError("busName is Null");
        val busNum = extras.getString("busNum") ?: throw AssertionError("busNum is Null");
        //set a loading screen first before displaying the correct buttons
        this.setContentView(R.layout.choose_direction);
        val busNumView: TextView = findViewById(R.id.chooseBusNum);
        val busNameView: TextView = findViewById(R.id.chooseBusDir);
        busNumView.text = busNum;
        busNameView.text = busName;

        setButtons(busNum)
    }

    private fun setButtons(busNum : String){
        if (busNum.toInt() <= 5){
            //todo
            Log.e("Dir Error", "No available buttons for the moment")
        }
        else{
            lifecycleScope.launch {
                val db = databaseBuilder(applicationContext, AppDatabase::class.java, "stm_info")
                    .createFromAsset("database/stm_info.db").build()
                val dirs = async {db.tripsDao().getTripHeadsigns(busNum.toInt())}.await()
                val dir0 = async { db.stopsInfoDao().getStopNames(dirs[0])}.await()
                val dir1 = async { db.stopsInfoDao().getStopNames(dirs.last()) }.await()
                val orientation = if (dirs[0].last() == 'E' || dirs[0].last() == 'O') Orientation.HORIZONTAL
                                    else Orientation.VERTICAL
                val headsign0 = dirs[0]
                val headsign1 = dirs[1]
                setListeners(orientation, dir0, dir1, headsign0, headsign1)
                db.close()
            }
        }
    }

    private suspend fun setListeners(orientation: Orientation, dir0 : List<String>, dir1 : List<String>,
                        headsign0 : String, headsign1: String){
        withContext(Dispatchers.Main){
            val leftButton : Button = findViewById(R.id.route_0)
            val leftDescr : TextView = findViewById(R.id.description_route_0)
            val rightButton : Button = findViewById(R.id.route_1)
            val rightDescr : TextView = findViewById(R.id.description_route_1)
            val intent = Intent(applicationContext, ChooseStop::class.java)
            when (orientation){
                Orientation.HORIZONTAL -> {
                    leftButton.text = "West"
                    rightButton.text = "East"

                }
                Orientation.VERTICAL -> {
                    leftButton.text = "North"
                    rightButton.text = "South"
                }
            }
            if (dir0[0].last() == 'W' || dir0[0].last() == 'N') {
                leftDescr.text = "From ${dir0[0]} to ${dir0.last()}"
                rightDescr.text = "From ${dir1[0]} to ${dir1.last()}"
            }
            else{
                leftDescr.text = "From ${dir1[0]} to ${dir1.last()}"
                rightDescr.text = "From ${dir0[0]} to ${dir0.last()}"
            }
            leftButton.setOnClickListener {
                intent.putStringArrayListExtra("stops", dir0 as ArrayList<String>)
                intent.putExtra("headsign", headsign0)
                startActivity(intent)
            }
            rightButton.setOnClickListener {
                intent.putStringArrayListExtra("stops", dir1 as ArrayList<String>)
                intent.putExtra("headsign", headsign1)
                startActivity(intent)
            }
        }
    }

    private enum class Orientation{
        HORIZONTAL,VERTICAL
    }
}
