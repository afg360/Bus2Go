package dev.mainhq.schedules;

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room.databaseBuilder
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.database.entities.Trips
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

//todo
//change appbar to be only a back button
//todo may make it a swapable ui instead of choosing button0 or 1
class ChooseBus : AppCompatActivity() {
    override fun onCreate(savedInstanceState : Bundle?) : Unit{
        super.onCreate(savedInstanceState);
        val extras : Bundle = this.intent.extras ?: throw AssertionError("Assertion failed");
        val busName = extras.getString("busName") ?: throw AssertionError("busName is Null");
        val busNum = extras.getString("busNum") ?: throw AssertionError("busNum is Null");
        //set a loading screen first before displaying the correct buttons
        this.setContentView(R.layout.choose_bus);
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
                val routes = db.tripsDao().getTripHeadsigns(busNum.toInt())
                //Log.d("Headsign", routes[0])
                //Log.d("ghol", routes.getAll().toString())
                setListeners(routes)
                db.close()
            }
        }
    }

    private suspend fun setListeners(routes : List<String>){
        withContext(Dispatchers.Main){
            val leftButton : Button = findViewById(R.id.route_0)
            val rightButton : Button = findViewById(R.id.route_1)
            val orientation : Orientation
            if (routes[0].last() == 'E' || routes[0].last() == 'O') {
                //todo depends on language!!
                leftButton.text = "West"
                rightButton.text = "East"
                orientation = Orientation.HORIZONTAL
            }
            else{
                leftButton.text = "North"
                rightButton.text = "South"
                orientation = Orientation.VERTICAL
            }
            //need to send other data
            leftButton.setOnClickListener {
                //todo get stop-times data where time is greater than
                //now and id is the same as this id
                var toSearch: String? = null
                if (orientation == Orientation.HORIZONTAL) {
                    for (route in routes) {
                        //ouest vs west
                        if (route.contains("O")) {
                            toSearch = route
                            break
                        }
                    }
                } else {
                    for (route in routes) {
                        if (route.contains("N")) {
                            toSearch = route
                            break
                        }
                    }
                }
                val intent = Intent(applicationContext, ChooseStop::class.java)
                intent.putExtra("BusLine", toSearch ?: throw IllegalArgumentException("Couldnt find busline!"))
                startActivity(intent)
            }
            rightButton.setOnClickListener{
                var toSearch : String? = null
                if (orientation == Orientation.HORIZONTAL) {
                    for (route in routes){
                        if (route.contains("E")) {
                            toSearch = route
                            break
                        }
                    }
                }
                else {
                    for (route in routes){
                        if (route.contains("S")) {
                            toSearch = route
                            break
                        }
                    }
                }
                val intent = Intent(applicationContext, ChooseStop::class.java)
                intent.putExtra("BusLine", toSearch ?: throw IllegalArgumentException("Couldnt find busline"))
                startActivity(intent)
            }
        }
    }
    private enum class Orientation{
        HORIZONTAL,VERTICAL
    }
}
