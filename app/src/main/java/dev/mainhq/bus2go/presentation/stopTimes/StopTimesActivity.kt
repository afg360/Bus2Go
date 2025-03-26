package dev.mainhq.bus2go.presentation.stopTimes

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.presentation.ui.adapters.TimeListElemsAdapter
import kotlinx.coroutines.launch
import android.os.Build
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.BaseActivity
import dev.mainhq.bus2go.BaseApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.ExoFavouriteTrainItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.StmFavouriteBusItem
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.Time


class StopTimesActivity : BaseActivity() {

    private var fromAlarmCreation = false


	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.times_activity)

        //TODO parcelised data
        @Suppress("DEPRECATION")
        val transitData = (if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra(BusExtrasInfo.TRANSIT_DATA.name, FavouriteTransitData::class.java)
            else  intent.getParcelableExtra(BusExtrasInfo.TRANSIT_DATA.name)) ?: throw IllegalStateException("You forgot to give a TransitData")

        val stopTimesViewModel: StopTimesViewModel by viewModels{
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    if (modelClass.isAssignableFrom(StopTimesViewModel::class.java)){
                        return StopTimesViewModel(
                            (this@StopTimesActivity.application as BaseApplication).appContainer.getTransitTime,
                            transitData
                        ) as T
                    }
                    throw IllegalArgumentException("Gave wrong ViewModel class")
                }
            }
        }

        fromAlarmCreation = intent.getBooleanExtra("ALARMS", false)

        val time = Time.now()
        val textView = findViewById<MaterialTextView>(R.id.time_title_text_view)

        @SuppressLint("SetTextI18n")
        when(transitData){
            is ExoFavouriteBusItem -> {
                textView.text = "${transitData.routeId} ${transitData.stopName} -> ${transitData.headsign}"
            }
            is ExoFavouriteTrainItem -> {
                textView.text = "#${transitData.trainNum} ${transitData.stopName} -> ${transitData.direction}"
            }
            is StmFavouriteBusItem -> {
                textView.text = "${transitData.routeId} ${transitData.stopName} -> ${transitData.direction}"
            }
        }

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val recyclerView: RecyclerView = findViewById(R.id.time_recycle_view)
        recyclerView.layoutManager = layoutManager
        val adapter = TimeListElemsAdapter(listOf(), fromAlarmCreation)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                stopTimesViewModel.arrivalTimes.collect { arrivalTimes ->
                    if (arrivalTimes.isEmpty()){
                        adapter.update(listOf())
                        recyclerView.visibility = View.GONE
                        val noTransitLeftTextView = findViewById<MaterialTextView>(R.id.no_available_transit_left_text_view)
                        noTransitLeftTextView.visibility = View.VISIBLE
                        noTransitLeftTextView.text
                    }
                    else{
                        adapter.update(arrivalTimes)
                    }
                }
            }
        }
    }
}