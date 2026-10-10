package app.bus2go.android.presentation.stop_times

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.os.Build
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import app.bus2go.android.presentation.base.BaseActivity
import app.bus2go.android.Bus2GoApplication
import app.bus2go.android.databinding.StopTimesActivityBinding
import app.bus2go.android.domain.entity.Time
import app.bus2go.android.domain.entity.TransitData
import app.bus2go.android.presentation.core.UiState
import app.bus2go.android.presentation.utils.ExtrasTagNames
import app.bus2go.android.utils.getMaterialColor
import app.bus2go.android.utils.launchViewModelCollectLatest
import app.bus2go.android.utils.makeGone
import app.bus2go.android.utils.makeInvisible
import app.bus2go.android.utils.makeVisible
import app.bus2go.android.utils.toEpochMillis
import app.bus2go.android.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.LocalDate


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
            setTextColor(getMaterialColor(stopTimesHeaderDisplayModel.routeIdTextColor))
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
            when(arrivalTimes) {
                UiState.Init -> {}
                UiState.Loading -> {
                    binding.timeCircularLoadingIndicator.makeVisible()
                    recyclerView.makeGone()
                    noTransitLeftTextView.makeGone()
                }
                is UiState.Success<List<StopTimesDisplayModel>> -> {
                    binding.timeCircularLoadingIndicator.makeGone()
                    if (arrivalTimes.data.isEmpty()){
                        adapter.update(listOf())
                        recyclerView.makeGone()
                        noTransitLeftTextView.makeVisible()
                    }
                    else{
                        adapter.update(arrivalTimes.data)
                        recyclerView.makeVisible()
                        noTransitLeftTextView.makeInvisible()
                    }
                }
                is UiState.Error -> {
                    binding.timeCircularLoadingIndicator.makeGone()
                    recyclerView.makeGone()
                    noTransitLeftTextView.makeGone()
                    toast(arrivalTimes.message)
                }
            }
        }
    }
}