package dev.mainhq.bus2go.presentation.choose_stop

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.data_source.local.database.exo.entity.StopTimes
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.presentation.Bus2GoApplication
import dev.mainhq.bus2go.presentation.stopTimes.StopTimesActivity
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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

        //terminus (i.e. to destination) = data.last(), needed for exo because some of the headsigns are the same
        val transitData = (if (Build.VERSION.SDK_INT >= 33) {
            intent.extras?.getParcelableArray(ExtrasTagNames.TRANSIT_DATA, TransitData::class.java)
        }
        else {
            @Suppress("DEPRECATION")
            intent.extras?.getParcelableArray(ExtrasTagNames.TRANSIT_DATA)
        })?.map { it as TransitData } ?: throw IllegalStateException("Expected to give a " + ExtrasTagNames.TRANSIT_DATA)

        val chooseStopViewModel: ChooseStopViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    if (modelClass.isAssignableFrom(ChooseStopViewModel::class.java)){
                        return ChooseStopViewModel(
                            transitData.toList(),
                            addFavourite = (application as Bus2GoApplication).appContainer.favouritesUseCases.addFavourite,
                            removeFavourite = (application as Bus2GoApplication).appContainer.favouritesUseCases.removeFavourite,
                            getFavourites = (application as Bus2GoApplication).appContainer.getFavourites,
                        ) as T
                    }
                    throw IllegalArgumentException("Gave wrong ViewModel class")
                }
            }
        }


        if (transitData.isNotEmpty()) {
            val recyclerView = findViewById<RecyclerView>(R.id.stop_recycle_view)
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.adapter = StopListElemsAdapter(transitData,
                addToFavouritesClickListener = {
                    if (chooseStopViewModel.favourites.value?.contains(it) == true){
                        chooseStopViewModel.removeFavourite(it)
                    }
                    else if (chooseStopViewModel.favourites.value?.contains(it) == false){
                        chooseStopViewModel.addFavourite(it)
                    }
                    else {
                        throw IllegalStateException("Clicking when favourites in the view model havent been init...")
                    }
                },
                onClickListener = {
                    val intent = Intent(this, StopTimesActivity::class.java)
                    intent.putExtra(ExtrasTagNames.TRANSIT_DATA, it)
                    startActivity(intent)
                }
            )
        }
        else{
            TODO("Display message saying no stops found")
        }
    }
}