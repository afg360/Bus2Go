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
import kotlin.IllegalStateException
import kotlin.collections.ArrayList
import dev.mainhq.bus2go.utils.BusExtrasInfo


//todo
//change appbar to be only a back button
//todo may make it a swapable ui instead of choosing button0 or 1
class ChooseDirection : BaseActivity() {

    private lateinit var agency : TransitAgency
    private lateinit var roomViewModel: RoomViewModel

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        roomViewModel = RoomViewModel(application)
        val routeName = intent.getStringExtra(BusExtrasInfo.ROUTE_NAME.name)!!
        val busNum = intent.getStringExtra(BusExtrasInfo.ROUTE_ID.name)!!
        agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(BusExtrasInfo.AGENCY.name, TransitAgency::class.java)!!
        } else {
            intent.getSerializableExtra(BusExtrasInfo.AGENCY.name) as TransitAgency
        }
        //set a loading screen first before displaying the correct buttons
        setContentView(R.layout.choose_direction)
        val busNumView: MaterialTextView = findViewById(R.id.chooseBusNum)
        val busNameView: MaterialTextView = findViewById(R.id.chooseBusDir)

        //todo need to ignore bus num for trains (use agency with route to check)
        if (agency == TransitAgency.EXO_TRAIN){
            val trainNum = intent.extras!!.getInt(BusExtrasInfo.TRAIN_NUM.name)
            busNumView.text = trainNum.toString()
            busNameView.text = routeName
            lifecycleScope.launch{
                val stopNames = roomViewModel.getTrainStopNames(this, busNum.toInt())
                val dir0 = stopNames.first.await()
                val dir1 = stopNames.second.await()
                //means to what station it is heading, NOT ACCURATE FOR THE MOMENT
                val headsign0 = dir0.last()
                val headsign1 = dir1.last()
                withContext(Dispatchers.Main){
                    val intent = Intent(applicationContext, ChooseStop::class.java)
                    findViewById<MaterialTextView>(R.id.description_route_0).text =
                        getString(R.string.train_direction, headsign0)
                    findViewById<MaterialButton>(R.id.route_0).setOnClickListener {
                        intent.putStringArrayListExtra("stops", dir0 as ArrayList<String>)
                        intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, 0)
                        intent.putExtra(BusExtrasInfo.DIRECTION.name, headsign0)
                        intent.putExtra(BusExtrasInfo.ROUTE_ID.name, busNum)
                        intent.putExtra(BusExtrasInfo.TRAIN_NUM.name, trainNum)
                        intent.putExtra(BusExtrasInfo.ROUTE_NAME.name, routeName)
                        intent.putExtra(BusExtrasInfo.AGENCY.name, agency)
                        startActivity(intent)
                    }

                    findViewById<MaterialTextView>(R.id.description_route_1).text =
                        getString(R.string.train_direction, headsign1)
                    findViewById<MaterialButton>(R.id.route_1).setOnClickListener {
                        intent.putStringArrayListExtra("stops", dir1 as ArrayList<String>)
                        intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, 1)
                        intent.putExtra(BusExtrasInfo.DIRECTION.name, headsign1)
                        intent.putExtra(BusExtrasInfo.ROUTE_ID.name, busNum)
                        intent.putExtra(BusExtrasInfo.TRAIN_NUM.name, trainNum)
                        intent.putExtra(BusExtrasInfo.ROUTE_NAME.name, routeName)
                        intent.putExtra(BusExtrasInfo.AGENCY.name, agency)
                        startActivity(intent)
                    }
                }
            }
        }
        else {
            busNumView.text = busNum
            busNameView.text = routeName
            setButtons(busNum, routeName)
        }
    }

    private fun setButtons(bus : String, routeName: String) {
        //if agency == EXO_Train, getTrips will actually give the "stops"
        //^need to check how to get the directions properly for trains...
        lifecycleScope.launch {
            val dirs = roomViewModel.getDirections(agency, bus)
            if (dirs.isEmpty()){
                Toast.makeText(this@ChooseDirection, "Metro not implemented yet...", Toast.LENGTH_SHORT).show()
                return@launch
            }
            //FIXME TRIPS_HEADSIGN HAS CHANGED FOR STM, ONLY SHOWS BusExtrasInfo.DIRECTION.name

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
                                //FIXME CHANGE BusExtrasInfo.DIRECTION.name TERMINOLOGY TO BE MORE CONSISTENT
                                leftButton.setOnClickListener {
                                    intent.putStringArrayListExtra("stops", stops1 as ArrayList<String>)
                                    intent.putExtra(BusExtrasInfo.ROUTE_ID.name, bus)//.toInt())
                                    intent.putExtra(BusExtrasInfo.LAST_STOP.name, stops1.last())
                                    intent.putExtra(BusExtrasInfo.DIRECTION.name, dirs.last().tripHeadSign)
                                    intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, dirs.last().directionId)
                                    intent.putExtra(BusExtrasInfo.AGENCY.name, agency)
                                    startActivity(intent)
                                }
                                rightButton.setOnClickListener {
                                    intent.putStringArrayListExtra("stops", stops0 as ArrayList<String>)
                                    intent.putExtra(BusExtrasInfo.ROUTE_ID.name, bus)//.toInt())
                                    intent.putExtra(BusExtrasInfo.LAST_STOP.name, stops0.last())
                                    intent.putExtra(BusExtrasInfo.DIRECTION.name, dirs.first().tripHeadSign)
                                    intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, dirs.first().directionId)
                                    intent.putExtra(BusExtrasInfo.AGENCY.name, agency)
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
                                    intent.putExtra(BusExtrasInfo.ROUTE_ID.name, bus)//.toInt())
                                    intent.putExtra(BusExtrasInfo.LAST_STOP.name, stops0.last())
                                    intent.putExtra(BusExtrasInfo.DIRECTION.name, dirs.first().tripHeadSign)
                                    intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, dirs.first().directionId)
                                    intent.putExtra(BusExtrasInfo.AGENCY.name, agency)
                                    startActivity(intent)
                                }
                                rightButton.setOnClickListener {
                                    intent.putStringArrayListExtra("stops", stops1 as ArrayList<String>)
                                    intent.putExtra(BusExtrasInfo.ROUTE_ID.name, bus)//.toInt())
                                    intent.putExtra(BusExtrasInfo.LAST_STOP.name, stops1.last())
                                    intent.putExtra(BusExtrasInfo.DIRECTION.name, dirs.last().tripHeadSign)
                                    intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, dirs.last().directionId)
                                    intent.putExtra(BusExtrasInfo.AGENCY.name, agency)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
                TransitAgency.EXO_OTHER -> {
                    val headsigns = dirs as List<String>
                    //some buses/transit only have 1 direction, so check before assigning
                    val intent = Intent(applicationContext, ChooseStop::class.java)
                    intent.putExtra(BusExtrasInfo.ROUTE_ID.name, bus)
                    intent.putExtra(BusExtrasInfo.ROUTE_NAME.name, routeName)

                    //Not a bug, if only 1 dir, then simply opens the activity...
                    if (headsigns.size == 1){
                        val dir = roomViewModel.getStopNames(this, headsigns[0], bus).await()
                        withContext(Dispatchers.Main) {
                            //no need for a button in this case
                            finish()
                            setIntent(intent, dir as ArrayList<String>, headsigns[0], dir.last(), agency)
                            Toast.makeText(this@ChooseDirection, "This bus line only contains 1 direction", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else if (dirs.size > 1){
                        val jobs = roomViewModel.getStopNames(this, agency, dirs, bus)
                        val headsign0 = headsigns[0]
                        val headsign1 = headsigns[1]
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
                    else {
                        throw IllegalStateException("Cannot have no directions for a transit")
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
        intent.putExtra(BusExtrasInfo.HEADSIGN.name, headsign)
        intent.putExtra(BusExtrasInfo.DIRECTION.name, direction)
        intent.putExtra(BusExtrasInfo.AGENCY.name, agency)
        startActivity(intent)
    }

    private enum class Orientation{
        HORIZONTAL,VERTICAL
    }
}
