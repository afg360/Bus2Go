package dev.mainhq.bus2go.presentation.choose_stop

import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.presentation.ui.adapters.StopListElemsAdapterExoOther
import dev.mainhq.bus2go.presentation.ui.adapters.StopListElemsAdapterExoTrain
import dev.mainhq.bus2go.presentation.ui.adapters.StopListElemsAdapterStm
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.presentation.main.home.favourites.FavouritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.IllegalStateException

//todo
//instead of doing a huge query on getting the time, we could first retrieve
//all the possible stations (ordered by id, and prob based on localisation? -> not privacy friendly
//and once the user clicks, either new activity OR new fragment? -> in the latter case need to implement onback
//todo add possibility of searching amongst all the stops
class ChooseStop() : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_stop)
        val favouritesViewModel = ViewModelProvider(this)[FavouritesViewModel::class.java]
        val loadingJob = lifecycleScope.async { favouritesViewModel.loadData() }

        //terminus (i.e. to destination) = data.last(), needed for exo because some of the headsigns are the same
        val stopNames : List<String> = intent.getStringArrayListExtra("stops") ?: listOf()

        val agency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra (ExtrasTagNames.AGENCY.name, TransitAgency::class.java) ?: throw AssertionError("AGENCY is Null")
        } else {
            intent.getSerializableExtra (ExtrasTagNames.AGENCY.name) as TransitAgency? ?: throw AssertionError("AGENCY is Null")
        }
        if (stopNames.isNotEmpty()) {
            val recyclerView: RecyclerView = findViewById(R.id.stop_recycle_view)
            val layoutManager = LinearLayoutManager(applicationContext)

            //non-nullable values
            val routeId = intent.getStringExtra(ExtrasTagNames.ROUTE_ID.name)!!
            val direction = intent.getStringExtra(ExtrasTagNames.DIRECTION.name)!!
            if (agency == TransitAgency.EXO_TRAIN || agency == TransitAgency.EXO_OTHER){
            }
            else if (agency == TransitAgency.STM && routeId.toInt() == -1)
                    throw IllegalStateException("Forgot to give a route id to an stm bus!")

            lifecycleScope.launch {
                //the datastore may be closed!
                loadingJob.await()
                val favourites = favouritesViewModel.stmBusInfo.value + favouritesViewModel.exoBusInfo.value +
                        favouritesViewModel.exoTrainInfo.value
                withContext(Dispatchers.Main){
                     when(agency){
                        TransitAgency.STM -> {
                            val directionId = intent.getIntExtra(ExtrasTagNames.DIRECTION_ID.name, -1)
                            if (directionId < 0) throw IllegalStateException("Forgot to give a direction Id to an Stm bus")
                            val lastStop = intent.getStringExtra(ExtrasTagNames.LAST_STOP.name) ?: throw IllegalStateException("Forgot to give a last stop to an Stm bus")
                            recyclerView.adapter = StopListElemsAdapterStm(stopNames, favourites,
                                favouritesViewModel, routeId, directionId, direction, lastStop)
                        }

                        TransitAgency.EXO_TRAIN -> {
                            val trainNum = intent.getIntExtra(ExtrasTagNames.TRAIN_NUM.name, -1)
                            if (trainNum < 0) throw IllegalStateException("No train num has been given to a train stop")
                            val directionId = intent.getIntExtra(ExtrasTagNames.DIRECTION_ID.name, -1)
                            if (directionId < 0) throw IllegalStateException("Forgot to give a direction Id to a train stop")
                            val routeName = intent.getStringExtra(ExtrasTagNames.ROUTE_NAME.name) ?: throw IllegalStateException("Forgot to give a route name to a train!")
                            recyclerView.adapter = StopListElemsAdapterExoTrain(stopNames, favourites,
                                favouritesViewModel, routeId, routeName, directionId, direction, trainNum)
                        }

                        TransitAgency.EXO_OTHER -> {
                            val headsign = intent.getStringExtra(ExtrasTagNames.HEADSIGN.name) ?: throw IllegalStateException("Forgot to give a headsign to an exo bus stop")
                            val routeName = intent.getStringExtra(ExtrasTagNames.ROUTE_NAME.name) ?: throw IllegalStateException("Forgot to give a route name to an exo bus!")
                            recyclerView.adapter = StopListElemsAdapterExoOther( stopNames, favourites,
                                favouritesViewModel, routeId, routeName, direction, headsign)
                        }
                    }
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