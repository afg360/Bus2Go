package dev.mainhq.bus2go

import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.adapters.StopListElemsAdapter
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

//todo
//instead of doing a huge query on getting the time, we could first retrieve
//all the possible stations (ordered by id, and prob based on localisation? -> not privacy friendly
//and once the user clicks, either new activity OR new fragment? -> in the latter case need to implement onback
//todo add possibility of searching amongst all the stops
class ChooseStop() : BaseActivity() {

    private lateinit var agency: TransitAgency
    private var directionId : Int? = null
    private var routeId : Int? = null
    private var trainNum : Int? = null
    private var routeName : String? = null
    private var headsign : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_stop)
        val favouritesViewModel = ViewModelProvider(this)[FavouritesViewModel::class.java]
        val loadingJob = lifecycleScope.async { favouritesViewModel.loadData() }

        //terminus (i.e. to destination) = data.last(), needed for exo because some of the headsigns are the same
        val data : List<String> = intent.getStringArrayListExtra("stops") ?: listOf()

        agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra (AGENCY, TransitAgency::class.java) ?: throw AssertionError("AGENCY is Null")
        } else {
            intent.getSerializableExtra (AGENCY) as TransitAgency? ?: throw AssertionError("AGENCY is Null")
        }
        if (data.isNotEmpty()) {
            val recyclerView: RecyclerView = findViewById(R.id.stop_recycle_view)
            val layoutManager = LinearLayoutManager(applicationContext)

            if (agency == TransitAgency.EXO_TRAIN){
                directionId = intent.getIntExtra(DIRECTION_ID, -1)
                if (directionId == -1) throw IllegalStateException("Forgot to give a direction id to a train!")
                routeId = intent.getIntExtra(ROUTE_ID, -1)
                if (routeId == -1) throw IllegalStateException("Forgot to give a route id to a train!")
                trainNum = intent.getIntExtra(TRAIN_NUM, -1)
                if (trainNum == -1) throw IllegalStateException("Forgot to give a train num to a train!")
                routeName = intent.getStringExtra(ROUTE_NAME) ?: throw IllegalStateException("Forgot to give a route name to a train!")
            }
            else{
                headsign = intent.getStringExtra("headsign")!!
            }

            lifecycleScope.launch {
                //the datastore may be closed!
                loadingJob.await()
                val favourites = favouritesViewModel.stmBusInfo.value + favouritesViewModel.exoBusInfo.value +
                        favouritesViewModel.exoTrainInfo.value
                withContext(Dispatchers.Main){
                    recyclerView.adapter = StopListElemsAdapter(data, favourites, headsign, routeId, trainNum, routeName, directionId, agency, favouritesViewModel)
                    layoutManager.orientation = LinearLayoutManager.VERTICAL
                    recyclerView.layoutManager = layoutManager
                }
            }
        }
        else{
            TODO("Display message saying no stops found")
        }
    }
}