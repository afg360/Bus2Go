package dev.mainhq.bus2go

import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.adapters.StopListElemsAdapter
import dev.mainhq.bus2go.utils.BusAgency
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

    private lateinit var agency: BusAgency
    private var directionId : Int? = null
    private var routeId : String? = null
    private var headsign : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_stop)
        val favouritesViewModel = ViewModelProvider(this)[FavouritesViewModel::class.java]
        val loadingJob = lifecycleScope.async { favouritesViewModel.loadData() }

        //terminus (i.e. to destination) = data.last(), needed for exo because some of the headsigns are the same
        val data : List<String> = intent.getStringArrayListExtra("stops") ?: listOf()

        agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra (AGENCY, BusAgency::class.java) ?: throw AssertionError("AGENCY is Null")
        } else {
            intent.getSerializableExtra (AGENCY) as BusAgency? ?: throw AssertionError("AGENCY is Null")
        }
        if (data.isNotEmpty()) {
            val recyclerView: RecyclerView = findViewById(R.id.stop_recycle_view)
            val layoutManager = LinearLayoutManager(applicationContext)

            if (agency == BusAgency.EXO_TRAIN){
                directionId = intent.getIntExtra(DIRECTION_ID, -1)
                if (directionId == -1) throw IllegalStateException("Forgot to give a direction id to a train!")
                routeId = intent.getStringExtra(ROUTE_ID) ?: throw IllegalStateException("Forgot to give a route id to a train!")
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
                    recyclerView.adapter = StopListElemsAdapter(data, favourites, headsign, routeId, directionId, agency, favouritesViewModel)
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