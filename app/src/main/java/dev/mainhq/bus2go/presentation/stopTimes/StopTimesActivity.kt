package dev.mainhq.bus2go.presentation.stopTimes

import android.annotation.SuppressLint
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
import dev.mainhq.bus2go.presentation.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import dev.mainhq.bus2go.domain.entity.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first


class StopTimesActivity : BaseActivity() {

    private var fromAlarmCreation = false


	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.times_activity)

        @Suppress("DEPRECATION")
        val transitData = (if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra(ExtrasTagNames.TRANSIT_DATA, TransitData::class.java)
            else  intent.getParcelableExtra(ExtrasTagNames.TRANSIT_DATA)) ?: throw IllegalStateException("You forgot to give a TransitData")

        val stopTimesViewModel: StopTimesViewModel by viewModels{
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    if (modelClass.isAssignableFrom(StopTimesViewModel::class.java)){
                        return StopTimesViewModel(
                            (this@StopTimesActivity.application as Bus2GoApplication).appContainer.getTransitTime,
                            transitData
                        ) as T
                    }
                    throw IllegalArgumentException("Gave wrong ViewModel class")
                }
            }
        }

        fromAlarmCreation = intent.getBooleanExtra("ALARMS", false)

        lifecycleScope.launch(Dispatchers.Main) {
            findViewById<MaterialTextView>(R.id.time_title_text_view).text = stopTimesViewModel.displayText.filterNotNull().first()
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