package dev.mainhq.bus2go;

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.database.stm_data.dao.DirectionInfo
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
const val DIRECTION = "DIRECTION"

/** Only for use with trains! */
/** Actual route id, as listed in the .txt files */
const val ROUTE_ID = "ROUTE_ID"
/** The number in the train route_long_name, e.g. %11% - blablabla */
const val TRAIN_NUM = "TRAIN_NUM"
const val DIRECTION_ID = "DIRECTION_ID"
/** For stm buses, the last stop */
const val LAST_STOP = "LAST_STOP"

//todo
//change appbar to be only a back button
//todo may make it a swapable ui instead of choosing button0 or 1
class ChooseDirection : BaseActivity() {

    private lateinit var agency : TransitAgency
    private lateinit var roomViewModel: RoomViewModel

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        roomViewModel = RoomViewModel(application)
        val routeName = intent.getStringExtra(ROUTE_NAME)!!
        val busNum = intent.getStringExtra(ROUTE_ID)!!
        agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(AGENCY, TransitAgency::class.java)!!
        } else {
            intent.getSerializableExtra(AGENCY) as TransitAgency
        }
        //set a loading screen first before displaying the correct buttons
        setContentView(R.layout.choose_direction)
        val busNumView: MaterialTextView = findViewById(R.id.chooseBusNum)
        val busNameView: MaterialTextView = findViewById(R.id.chooseBusDir)

        //todo need to ignore bus num for trains (use agency with route to check)
        if (agency == TransitAgency.EXO_TRAIN){
            val trainNum = intent.extras!!.getInt(TRAIN_NUM)
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
                //means to what station it is heading, NOT ACCURATE FOR THE MOMENT
                val headsign0 = dir0.last()
                val headsign1 = dir1.last()
                withContext(Dispatchers.Main){
                    val intent = Intent(applicationContext, ChooseStop::class.java)
                    findViewById<MaterialTextView>(R.id.description_route_0).text = headsign0
                    findViewById<MaterialButton>(R.id.route_0).setOnClickListener {
                        intent.putStringArrayListExtra("stops", dir0 as ArrayList<String>)
                        intent.putExtra(DIRECTION_ID, 0)
                        intent.putExtra(DIRECTION, headsign0)
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
                        intent.putExtra(DIRECTION, headsign1)
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
        //if agency == EXO_Train, getTrips will actually give the "stops"
        //^need to check how to get the directions properly for trains...
        lifecycleScope.launch {
            val dirs = roomViewModel.getDirections(agency, bus)
            if (dirs.isEmpty()){
                Toast.makeText(this@ChooseDirection, "Metro not implemented yet...", Toast.LENGTH_SHORT).show()
                return@launch
            }
            //FIXME TRIPS_HEADSIGN HAS CHANGED FOR STM, ONLY SHOWS DIRECTION


            when (agency) {
                TransitAgency.STM -> {
                    dirs as List<DirectionInfo>
                    val jobs = roomViewModel.getStopNames(this, agency, dirs.map { it.tripHeadSign }, bus)
                    val orientation =
                        //already in alphabetical order
                        if (dirs.first().tripHeadSign == "Est" || dirs.first().tripHeadSign == "Ouest") Orientation.HORIZONTAL
                        else Orientation.VERTICAL
                    withContext(Dispatchers.Main){
                        val leftButton : MaterialButton = findViewById(R.id.route_0)
                        val leftDescr : MaterialTextView = findViewById(R.id.description_route_0)
                        val rightButton : MaterialButton = findViewById(R.id.route_1)
                        val rightDescr : MaterialTextView = findViewById(R.id.description_route_1)
                        val intent = Intent(applicationContext, ChooseStop::class.java)
                        //east or north
                        val stops0 = jobs.first.await()
                        //west or south
                        val stops1 = jobs.second.await()
                        when (orientation){
                            Orientation.HORIZONTAL -> {
                                leftButton.text = getString(R.string.west)
                                rightButton.text = getString(R.string.east)
                                leftDescr.text = getString(R.string.from_to, stops1.first(), stops1.last())
                                rightDescr.text = getString(R.string.from_to, stops0.first(), stops0.last())
                                //FIXME CHANGE DIRECTION TERMINOLOGY TO BE MORE CONSISTENT
                                leftButton.setOnClickListener {
                                    intent.putStringArrayListExtra("stops", stops1 as ArrayList<String>)
                                    intent.putExtra(ROUTE_ID, bus)//.toInt())
                                    intent.putExtra(LAST_STOP, stops1.last())
                                    intent.putExtra(DIRECTION, dirs.last().tripHeadSign)
                                    intent.putExtra(DIRECTION_ID, dirs.last().directionId)
                                    intent.putExtra(AGENCY, agency)
                                    startActivity(intent)
                                }
                                rightButton.setOnClickListener {
                                    intent.putStringArrayListExtra("stops", stops0 as ArrayList<String>)
                                    intent.putExtra(ROUTE_ID, bus)//.toInt())
                                    intent.putExtra(LAST_STOP, stops0.last())
                                    intent.putExtra(DIRECTION, dirs.first().tripHeadSign)
                                    intent.putExtra(DIRECTION_ID, dirs.first().directionId)
                                    intent.putExtra(AGENCY, agency)
                                    startActivity(intent)
                                }
                            }
                            Orientation.VERTICAL -> {
                                leftButton.text = getString(R.string.north)
                                rightButton.text = getString(R.string.south)

                                leftDescr.text = getString(R.string.from_to, stops0.first(), stops0.last())
                                rightDescr.text = getString(R.string.from_to, stops1.first(), stops1.last())
                                leftButton.setOnClickListener {
                                    intent.putStringArrayListExtra("stops", stops0 as ArrayList<String>)
                                    intent.putExtra(ROUTE_ID, bus)//.toInt())
                                    intent.putExtra(LAST_STOP, stops0.last())
                                    intent.putExtra(DIRECTION, dirs.first().tripHeadSign)
                                    intent.putExtra(DIRECTION_ID, dirs.first().directionId)
                                    intent.putExtra(AGENCY, agency)
                                    startActivity(intent)
                                }
                                rightButton.setOnClickListener {
                                    intent.putStringArrayListExtra("stops", stops1 as ArrayList<String>)
                                    intent.putExtra(ROUTE_ID, bus)//.toInt())
                                    intent.putExtra(LAST_STOP, stops1.last())
                                    intent.putExtra(DIRECTION, dirs.last().tripHeadSign)
                                    intent.putExtra(DIRECTION_ID, dirs.last().directionId)
                                    intent.putExtra(AGENCY, agency)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
                TransitAgency.EXO_OTHER -> {
                    dirs as List<String>
                    val jobs = roomViewModel.getStopNames(this, agency, dirs, bus)
                    val headsign0 = dirs[0]
                    val headsign1 = dirs[1]
                    val intent = Intent(applicationContext, ChooseStop::class.java)
                    val dir0 = jobs.first.await()
                    val dir1 = jobs.second.await()
                    withContext(Dispatchers.Main) {
                        findViewById<MaterialTextView>(R.id.description_route_0).text = headsign0
                        findViewById<MaterialButton>(R.id.route_0).setOnClickListener {
                            setIntent(intent, dir0 as ArrayList<String>, headsign0, dir0.last(), agency)
                        }
                        findViewById<MaterialTextView>(R.id.description_route_1).text = headsign1
                        findViewById<MaterialButton>(R.id.route_1).setOnClickListener {
                            setIntent(intent, dir1 as ArrayList<String>, headsign1, dir1.last(), agency)
                        }
                    }
                }
                else -> {
                    throw IllegalStateException("Wrong agency given for getting directions!")
                }
            }
        }
    }

    private fun setIntent(intent : Intent, dir : ArrayList<String>, headsign: String, direction : String, agency: TransitAgency){
        intent.putStringArrayListExtra("stops", dir)
        intent.putExtra("headsign", headsign)
        intent.putExtra(DIRECTION, direction)
        intent.putExtra(AGENCY, agency)
        startActivity(intent)
    }

    private enum class Orientation{
        HORIZONTAL,VERTICAL
    }
}
