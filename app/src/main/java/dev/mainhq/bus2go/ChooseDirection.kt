package dev.mainhq.bus2go;

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.utils.BusAgency
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.ArrayList

const val BUS_NAME = "BUS_NAME"
const val BUS_NUM = "BUS_NUM"
const val AGENCY = "AGENCY"
/** Only for use with trains! */
const val ROUTE_ID = "ROUTE_ID"
const val DIRECTION_ID = "DIRECTION_ID"

//todo
//change appbar to be only a back button
//todo may make it a swapable ui instead of choosing button0 or 1
class ChooseDirection : BaseActivity() {

    private lateinit var agency : BusAgency
    private lateinit var roomViewModel: RoomViewModel

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        roomViewModel = RoomViewModel(application)
        val extras : Bundle = this.intent.extras ?: throw AssertionError("Assertion failed")
        val busName = extras.getString(BUS_NAME) ?: throw AssertionError("BUS_NAME is Null")
        val busNum = extras.getString(BUS_NUM) ?: throw AssertionError("BUS_NUM is Null")
        agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getSerializable (AGENCY, BusAgency::class.java) ?: throw AssertionError("AGENCY is Null")
        } else {
            extras.getSerializable (AGENCY) as BusAgency? ?: throw AssertionError("AGENCY is Null")
        }
        //set a loading screen first before displaying the correct buttons
        setContentView(R.layout.choose_direction)
        val busNumView: MaterialTextView = findViewById(R.id.chooseBusNum)
        val busNameView: MaterialTextView = findViewById(R.id.chooseBusDir)
        busNumView.text = busNum;
        busNameView.text = busName;
        //todo need to ignore bus num for trains (use agency with route to check)
        if (agency == BusAgency.EXO_TRAIN){
            lifecycleScope.launch{
                //directions == 0 or 1 (used in the queries, may be different???)
                val routeId = try {
                    busNum.toInt()
                }
                catch (e : TypeCastException){
                    throw TypeCastException("In choose direction, when a train, cannot cast a non-integer route id to an integer!")
                }

                val stopNames = roomViewModel.getTrainStopNames(this, routeId)
                val dir0 = stopNames.first.await()
                val dir1 = stopNames.second.await()
                //means to what station it is heading
                val headsign0 = dir0.last()
                val headsign1 = dir1.last()
                withContext(Dispatchers.Main){
                    val intent = Intent(applicationContext, ChooseStop::class.java)
                    findViewById<MaterialTextView>(R.id.description_route_0).text = headsign0 //FIXME???
                    findViewById<MaterialButton>(R.id.route_0).setOnClickListener {
                        intent.putStringArrayListExtra("stops", dir0 as ArrayList<String>)
                        intent.putExtra(DIRECTION_ID, 0)
                        intent.putExtra(ROUTE_ID, routeId.toString())
                        intent.putExtra(AGENCY, agency)
                        startActivity(intent)
                    }
                    findViewById<MaterialTextView>(R.id.description_route_1).text = headsign1
                    findViewById<MaterialButton>(R.id.route_1).setOnClickListener {
                        intent.putStringArrayListExtra("stops", dir1 as ArrayList<String>)
                        intent.putExtra(DIRECTION_ID, 1)
                        intent.putExtra(ROUTE_ID, routeId.toString())
                        intent.putExtra(AGENCY, agency)
                        startActivity(intent)
                    }
                }
            }
        }
        else {
            setButtons(busNum)
        }
    }

    private fun setButtons(bus : String) {
        //if agency == EXO_Train, getTrips will actuvally give the "stops"
        //^need to check how to get the directions properly for trains...
        lifecycleScope.launch {
            val dirs = roomViewModel.getDirections(agency, bus)
            if (dirs.isEmpty()){
                Toast.makeText(this@ChooseDirection, "Metro not implemented yet...", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val jobs = roomViewModel.getStopNames(this, agency, dirs)
            val headsign0 = dirs[0]
            val headsign1 = dirs[1]
            when (agency) {
                BusAgency.STM -> {
                    val orientation =
                        if (dirs[0].last() == 'E' || dirs[0].last() == 'O') Orientation.HORIZONTAL
                        else Orientation.VERTICAL
                    setListeners(orientation, jobs.first.await(), jobs.second.await(), headsign0, headsign1)
                }
                BusAgency.EXO_OTHER -> {
                    val intent = Intent(applicationContext, ChooseStop::class.java)
                    val dir0 = jobs.first.await()
                    val dir1 = jobs.second.await()
                    withContext(Dispatchers.Main) {
                        findViewById<MaterialTextView>(R.id.description_route_0).text = headsign0
                        findViewById<MaterialButton>(R.id.route_0).setOnClickListener {
                            intent.putStringArrayListExtra("stops", dir0 as ArrayList<String>)
                            intent.putExtra("headsign", headsign0)
                            intent.putExtra(AGENCY, agency)
                            startActivity(intent)
                        }
                        findViewById<MaterialTextView>(R.id.description_route_1).text = headsign1
                        findViewById<MaterialButton>(R.id.route_1).setOnClickListener {
                            intent.putStringArrayListExtra("stops", dir1 as ArrayList<String>)
                            intent.putExtra("headsign", headsign1)
                            intent.putExtra(AGENCY, agency)
                            startActivity(intent)
                        }
                    }
                }
                else -> {
                    throw IllegalStateException("Wrong agency given for getting directions!")
                }
            }
        }
    }

    private suspend fun setListeners(orientation: Orientation, dir0 : List<String>, dir1 : List<String>,
                                     headsign0 : String, headsign1: String){
        withContext(Dispatchers.Main){
            val leftButton : MaterialButton = findViewById(R.id.route_0)
            val leftDescr : MaterialTextView = findViewById(R.id.description_route_0)
            val rightButton : MaterialButton = findViewById(R.id.route_1)
            val rightDescr : MaterialTextView = findViewById(R.id.description_route_1)
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
                leftButton.setOnClickListener {
                    intent.putStringArrayListExtra("stops", dir0 as ArrayList<String>)
                    intent.putExtra("headsign", headsign0)
                    intent.putExtra(AGENCY, agency)
                    startActivity(intent)
                }
                rightButton.setOnClickListener {
                    intent.putStringArrayListExtra("stops", dir1 as ArrayList<String>)
                    intent.putExtra("headsign", headsign1)
                    intent.putExtra(AGENCY, agency)
                    startActivity(intent)
                }
            }
            else{
                leftDescr.text = "From ${dir1[0]} to ${dir1.last()}"
                rightDescr.text = "From ${dir0[0]} to ${dir0.last()}"
                leftButton.setOnClickListener {
                    intent.putStringArrayListExtra("stops", dir1 as ArrayList<String>)
                    intent.putExtra("headsign", headsign1)
                    intent.putExtra(AGENCY, agency)
                    startActivity(intent)
                }
                rightButton.setOnClickListener {
                    intent.putStringArrayListExtra("stops", dir0 as ArrayList<String>)
                    intent.putExtra("headsign", headsign0)
                    intent.putExtra(AGENCY, agency)
                    startActivity(intent)
                }
            }
        }
    }

    private enum class Orientation{
        HORIZONTAL,VERTICAL
    }
}
