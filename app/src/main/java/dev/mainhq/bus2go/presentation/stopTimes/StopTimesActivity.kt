package dev.mainhq.bus2go.presentation.stopTimes

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.os.Build
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first


class StopTimesActivity : BaseActivity() {

    private var fromAlarmCreation = false


	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stop_times_activity)

        @Suppress("DEPRECATION")
        val transitData = (if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra(ExtrasTagNames.TRANSIT_DATA, TransitData::class.java)
            else  intent.getParcelableExtra(ExtrasTagNames.TRANSIT_DATA))
            ?: throw IllegalStateException("You forgot to give a TransitData")

        val stopTimesViewModel: StopTimesViewModel by viewModels{
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    if (modelClass.isAssignableFrom(StopTimesViewModel::class.java)){
                        return StopTimesViewModel(
                            (this@StopTimesActivity.application as Bus2GoApplication).commonModule.getTransitTime,
                            transitData
                        ) as T
                    }
                    throw IllegalArgumentException("Gave wrong ViewModel class")
                }
            }
        }

        fromAlarmCreation = intent.getBooleanExtra("ALARMS", false)

        lifecycleScope.launch(Dispatchers.Main) {
            //FIXME there seems to be some delay when displaying the header...
            val stopTimesHeaderDisplayModel = stopTimesViewModel.stopTimesHeaderDisplayModel.filterNotNull().first()
            findViewById<MaterialTextView>(R.id.time_transit_route_id_text_view).also{
                it.text = stopTimesHeaderDisplayModel.routeIdText
                it.textSize = stopTimesHeaderDisplayModel.routeIdTextSize
                it.setTextColor(resources.getColor(stopTimesHeaderDisplayModel.routeIdTextColor, null))
            }
            //FIXME use string resources to say "to blablabla"
            findViewById<MaterialTextView>(R.id.time_direction_text_view).text = stopTimesHeaderDisplayModel.directionText
            findViewById<MaterialTextView>(R.id.time_stop_name_text_view).text = stopTimesHeaderDisplayModel.stopNameText
        }

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val recyclerView: RecyclerView = findViewById(R.id.time_recycle_view)
        recyclerView.layoutManager = layoutManager
        val adapter = StopTimeListElemsAdapter(listOf(), fromAlarmCreation)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                stopTimesViewModel.arrivalTimes.filterNotNull().collect{ arrivalTimes ->
                    val noTransitLeftTextView = findViewById<MaterialTextView>(R.id.no_available_transit_left_text_view)
                    if (arrivalTimes.isEmpty()){
                        adapter.update(listOf())
                        recyclerView.visibility = View.GONE
                        noTransitLeftTextView.visibility = VISIBLE
                    }
                    else{
                        adapter.update(arrivalTimes)
                        recyclerView.visibility = VISIBLE
                        noTransitLeftTextView.visibility = INVISIBLE
                    }
                }
            }
        }
    }
}