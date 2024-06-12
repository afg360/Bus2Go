package dev.mainhq.bus2go;

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.ArrayList

const val ROUTE_NAME = "BUS_NAME"
const val BUS_NUM = "BUS_NUM"
const val AGENCY = "AGENCY"

/** Only for use with trains! */
/** Actual route id, as listed in the .txt files */
const val ROUTE_ID = "ROUTE_ID"
/** The number in the train route_long_name, e.g. %11% - blablabla */
const val TRAIN_NUM = "TRAIN_NUM"
const val DIRECTION_ID = "DIRECTION_ID"

//todo
//change appbar to be only a back button
//todo may make it a swapable ui instead of choosing button0 or 1
class ChooseDirection : BaseActivity() {

    private lateinit var agency : TransitAgency
    private lateinit var roomViewModel: RoomViewModel

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        roomViewModel = RoomViewModel(application)
        val routeName = intent.extras!!.getString(ROUTE_NAME) ?: throw AssertionError("ROUTE_NAME is Null")
        val busNum = intent.extras!!.getString(BUS_NUM) ?: throw AssertionError("BUS_NUM is Null")
        agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras!!.getSerializable (AGENCY, TransitAgency::class.java) ?: throw AssertionError("AGENCY is Null")
        } else {
            intent.extras!!.getSerializable (AGENCY) as TransitAgency? ?: throw AssertionError("AGENCY is Null")
        }
        //set a loading screen first before displaying the correct buttons
        setContentView(R.layout.choose_direction)
        val busNumView: MaterialTextView = findViewById(R.id.chooseBusNum)
        val busNameView: MaterialTextView = findViewById(R.id.chooseBusDir)

        //todo need to ignore bus num for trains (use agency with route to check)
        if (agency == TransitAgency.EXO_TRAIN){
            val trainNum = intent.getIntExtra(TRAIN_NUM, -1)
            if (trainNum == -1) throw IllegalStateException("Forgot to give a train number to a train!")
            busNumView.text = trainNum.toString()
            busNameView.text = routeName
            val routeId = try {
                busNum.toInt()
            }
            catch (e : TypeCastException){
                throw TypeCastException("In choose direction, when a train, cannot cast a non-integer route id to an integer!")
            }
            lifecycleScope.launch{
                val stopNames = roomViewModel.getTrainStopNames(this, routeId)
                val dir0 = stopNames.first.await()
                val dir1 = stopNames.second.await()
                //means to what station it is heading
                val headsign0 = dir0.last()
                val headsign1 = dir1.last()
                withContext(Dispatchers.Main){
                    val intent = Intent(applicationContext, ChooseStop::class.java)
                    findViewById<MaterialTextView>(R.id.description_route_0).text = headsign0
                    findViewById<MaterialButton>(R.id.route_0).setOnClickListener {
                        intent.putStringArrayListExtra("stops", dir0 as ArrayList<String>)
                        intent.putExtra(DIRECTION_ID, 0)
                        intent.putExtra(ROUTE_ID, routeId)
                        intent.putExtra(TRAIN_NUM, trainNum)
                        intent.putExtra(ROUTE_NAME, routeName)
                        intent.putExtra(AGENCY, agency)
                        startActivity(intent)
                    }
                    findViewById<MaterialTextView>(R.id.description_route_1).text = headsign1
                    findViewById<MaterialButton>(R.id.route_1).setOnClickListener {
                        intent.putStringArrayListExtra("stops", dir1 as ArrayList<String>)
                        intent.putExtra(DIRECTION_ID, 1)
                        intent.putExtra(ROUTE_ID, routeId)
                        intent.putExtra(TRAIN_NUM, trainNum)
                        intent.putExtra(ROUTE_NAME, routeName)
                        intent.putExtra(AGENCY, agency)
                        startActivity(intent)
                    }
                }
            }
        }
        else {
            busNumView.text = busNum
            busNameView.text = routeName
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
                TransitAgency.STM -> {
                    val orientation =
                        if (dirs[0].last() == 'E' || dirs[0].last() == 'O') Orientation.HORIZONTAL
                        else Orientation.VERTICAL
                    setListeners(orientation, jobs.first.await(), jobs.second.await(), headsign0, headsign1)
                }
                TransitAgency.EXO_OTHER -> {
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
                    leftButton.text = getString(R.string.west)
                    rightButton.text = getString(R.string.east)
                }
                Orientation.VERTICAL -> {
                    leftButton.text = getString(R.string.north)
                    rightButton.text = getString(R.string.south)
                }
            }
            if (dir0[0].last() == 'W' || dir0[0].last() == 'N') {
                leftDescr.text = getString(R.string.from_to, dir0[0], dir0.last())
                rightDescr.text = getString(R.string.from_to, dir1[0], dir1.last())
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
                leftDescr.text = getString(R.string.from_to, dir1[0], dir1.last())
                rightDescr.text = getString(R.string.from_to, dir0[0], dir0.last())
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
