package dev.mainhq.bus2go.presentation.stop_times

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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.databinding.MainActivityBinding
import dev.mainhq.bus2go.databinding.StopTimesActivityBinding
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest
import dev.mainhq.bus2go.utils.makeGone
import dev.mainhq.bus2go.utils.makeInvisible
import dev.mainhq.bus2go.utils.makeVisible
import dev.mainhq.bus2go.utils.toEpochDay
import dev.mainhq.bus2go.utils.toEpochMillis
import dev.mainhq.bus2go.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.coroutineContext


class StopTimesActivity : BaseActivity() {

    private var fromAlarmCreation = false

    private lateinit var binding: StopTimesActivityBinding

	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StopTimesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val transitData = (if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra(ExtrasTagNames.TRANSIT_DATA, TransitData::class.java)
            else  intent.getParcelableExtra(ExtrasTagNames.TRANSIT_DATA))
            ?: throw IllegalStateException("You forgot to give a TransitData")

        val stopTimesViewModel: StopTimesViewModel by viewModels{
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    if (modelClass.isAssignableFrom(StopTimesViewModel::class.java)){
                        @Suppress("UNCHECKED_CAST")
                        return StopTimesViewModel(
                            transitData,
                            (this@StopTimesActivity.application as Bus2GoApplication).commonModule.getTransitTime,
                            (this@StopTimesActivity.application as Bus2GoApplication).commonModule.getDatabaseExpiryDate
                        ) as T
                    }
                    throw IllegalArgumentException("Gave wrong ViewModel class")
                }
            }
        }

        fromAlarmCreation = intent.getBooleanExtra("ALARMS", false)

        val stopTimesHeaderDisplayModel = stopTimesViewModel.stopTimesHeaderDisplayModel
        binding.timeTransitRouteIdTextView.apply {
            text = stopTimesHeaderDisplayModel.routeIdText
            textSize = stopTimesHeaderDisplayModel.routeIdTextSize
            setTextColor(resources.getColor(stopTimesHeaderDisplayModel.routeIdTextColor, null))
        }
        binding.timeDirectionTextView.text = stopTimesHeaderDisplayModel.directionText
        binding.timeStopNameTextView.text = stopTimesHeaderDisplayModel.stopNameText

        val layoutManager = LinearLayoutManager(applicationContext).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        val recyclerView: RecyclerView = binding.timeRecycleView.apply {
            this.layoutManager = layoutManager
        }

        val adapter = StopTimeListElemsAdapter(listOf(), fromAlarmCreation)
        recyclerView.adapter = adapter

        launchViewModelCollectLatest(stopTimesViewModel.chosenDate) {
            //Aug. 01, 2028
            binding.timeDatePickerTextView.text = it?.getDateOfYearString() ?: Time.now().getDateOfYearString()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            val maxCalendarDate = stopTimesViewModel.maxCalendarDate.filterNotNull().first().toEpochMillis()

            binding.timeDatePickerLayout.setOnClickListener {
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Choose a date")
                    .setCalendarConstraints(
                        CalendarConstraints.Builder()
                            .setValidator(
                                CompositeDateValidator.allOf(
                                    listOf(
                                        DateValidatorPointForward.from(
                                            LocalDate.now().toEpochMillis()
                                        ),
                                        DateValidatorPointBackward.before(
                                            maxCalendarDate
                                        )
                                    )
                                )
                            )
                            .build()
                    )
                    .setPositiveButtonText("Confirm")
                    .setNegativeButtonText("Cancel")
                    .build().also { dialog ->
                        dialog.addOnPositiveButtonClickListener {
                            stopTimesViewModel.setChosenDate(it)
                        }
                        dialog.addOnNegativeButtonClickListener {
                            dialog.dismiss()
                        }
                    }
                    .show(supportFragmentManager, null)
            }
        }


        launchViewModelCollectLatest(stopTimesViewModel.arrivalTimes) { arrivalTimes ->
            val noTransitLeftTextView = binding.noAvailableTransitLeftTextView
            if (arrivalTimes.isEmpty()){
                adapter.update(listOf())
                recyclerView.makeGone()
                noTransitLeftTextView.makeVisible()
            }
            else{
                adapter.update(arrivalTimes)
                recyclerView.makeVisible()
                noTransitLeftTextView.makeInvisible()
            }
        }
    }
}