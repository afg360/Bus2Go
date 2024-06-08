package dev.mainhq.bus2go.fragments

import java.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import dev.mainhq.bus2go.MainActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.viewmodel.AlarmCreationViewModel
import dev.mainhq.bus2go.viewmodel.FavouritesViewModel
import java.lang.IllegalStateException


private const val UNSELECTED = "UNSELECTED"
private const val SELECTED = "SELECTED"

class AlarmCreationDialog(private val alarmCreationViewModel : AlarmCreationViewModel,
    private val favouritesViewModel: FavouritesViewModel) : DialogFragment(R.layout.fragment_create_alarms_dialog) {

    private lateinit var bottomNavBar: AlarmCreationDialogBottomNavBar

    private var allZero = true
    private var allUnselected = true
    private lateinit var beforeTime : Time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MaterialTheme_Bus2Go_Dark)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialTextView>(R.id.textViewChooseStop).setOnClickListener { _ ->
            val alarmCreationChooseBusDialog = AlarmCreationChooseBusDialog(favouritesViewModel)
            //TODO DEPRECATED FOR DATA EXCHANGE alarmCreationDialog.setTargetFragment(this@Alarms, 0)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.add(alarmCreationChooseBusDialog, null).commit()
            setFragmentResultListener("requestKey") { requestKey, bundle ->
                if (Build.VERSION.SDK_INT < 33) {
                    alarmCreationViewModel.updateAlarmBusInfo(bundle.getParcelable("ALARM_BUS_INFO")!!)
                } else {
                    alarmCreationViewModel.updateAlarmBusInfo(bundle.getParcelable("ALARM_BUS_INFO",
                        AlarmCreationChooseBusDialog.AlarmBusInfo::class.java)!!)
                }
                alarmCreationViewModel.alarmBusInfo.value?.also {
                    Log.d("ALARM BUS", it.toString())
                    view.findViewById<LinearLayout>(R.id.alarmCreationHiddenThings).visibility = View.VISIBLE
                    view.findViewById<MaterialTextView>(R.id.textViewChooseStop).text = it.busInfo.toString()

                    /** Create time before picker */
                    val chooseTimeTextView = view.findViewById<MaterialTextView>(R.id.chooseTimeTextView)

                    chooseTimeTextView.setOnClickListener {
                        val timePicker = MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setMinute(0)
                            .setTitleText("Select Time Before The Bus")
                            .setInputMode(INPUT_MODE_KEYBOARD)
                            .build()

                        timePicker.addOnCancelListener {
                            it.cancel()
                        }
                        timePicker.addOnPositiveButtonClickListener {
                            chooseTimeTextView.text =
                                "${timePicker.hour} h, ${timePicker.minute} min before choosen bus time"
                            allZero = timePicker.hour == 0 && timePicker.minute == 0
                            if (!allUnselected && !allZero) {
                                bottomNavBar.activateAcceptAlarmButton()
                                beforeTime = Time(timePicker.hour, timePicker.minute, 0)
                            }
                            else bottomNavBar.deActivateAcceptAlarmButton()
                        }
                        timePicker.show(parentFragmentManager, null)
                    }

                    /** Create day of the week picker */
                    view.findViewById<LinearLayout>(R.id.chooseDatesRadios).children.forEach {
                        //For each Radio button
                        val radioButton = (it as RelativeLayout)[0] as RadioButton
                        radioButton.tag = UNSELECTED
                        radioButton.setOnClickListener { _ ->
                            allUnselected = true
                            if (radioButton.tag == UNSELECTED) {
                                radioButton.tag = SELECTED
                                allUnselected = false
                            }
                            else {
                                radioButton.isChecked = false
                                radioButton.tag = UNSELECTED
                                //TODO if all are also unselected, cannot accept the dialog
                                view.findViewById<LinearLayout>(R.id.chooseDatesRadios).children.forEach{
                                    innerLoopRelativeLayout ->
                                    if (((innerLoopRelativeLayout as RelativeLayout)[0] as RadioButton).tag == SELECTED){
                                        allUnselected = false
                                    }
                                }
                            }
                            //FIXME put the below inside a listener to run it when the state changes, perhaps inside a viewModel
                            if (!allUnselected && !allZero) bottomNavBar.activateAcceptAlarmButton()
                            else bottomNavBar.deActivateAcceptAlarmButton()
                        }
                    }
                }
            }
        }

        /*
        /** Create date picker  */
        val chooseDateTextView = view.findViewById<MaterialTextView>(R.id.chooseDateTextView)
        chooseDateTextView.setOnClickListener{
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Days when the alarm is active")
                .build()
            //datePicker.addOnPositiveButtonClickListener {
            //    datePicker
            //}
            datePicker.show(parentFragmentManager, null)
        }
         */

        bottomNavBar = AlarmCreationDialogBottomNavBar()
        bottomNavBar.setBottomNavBarListener(object :
            AlarmCreationDialogBottomNavBar.BottomNavBarListener {
            override fun onCancel() {
                dismiss()
            }

            override fun onAccept() {
                //TODO save the data, create a new alarm (goes inside alarms.json), and dismiss
                this@AlarmCreationDialog.apply{
                    //could assert non-null
                    val map = (alarmCreationViewModel.chosenDays.value)?.toMutableMap() ?: mutableMapOf()
                    view.findViewById<LinearLayout>(R.id.chooseDatesRadios).children.forEach{
                        val radioButton = (it as ViewGroup)[0] as RadioButton
                        when(radioButton.id){
                            R.id.sundayRadio -> if (radioButton.isChecked) map.replace('d', true)
                            R.id.mondayRadio -> if (radioButton.isChecked) map.replace('m', true)
                            R.id.tuesdayRadio -> if (radioButton.isChecked) map.replace('t', true)
                            R.id.wednesdayRadio -> if (radioButton.isChecked) map.replace('w', true)
                            R.id.thursdayRadio -> if (radioButton.isChecked) map.replace('y', true)
                            R.id.fridayRadio -> if (radioButton.isChecked) map.replace('f', true)
                            R.id.saturdayRadio -> if (radioButton.isChecked) map.replace('s', true)
                            else -> throw IllegalStateException("There cannot be another radio button for the days!")
                        }
                    }
                    alarmCreationViewModel.updateDays(map)
                    alarmCreationViewModel.updateBeforeTime(beforeTime)
                    // NAMING of an alarm: alarmID.dayOfWeek
                    map.forEach { (day, isOn) ->
                        /* Need to setup an alarm for every day on! */
                        if (isOn){
                            Calendar.getInstance().apply {
                                when(day){
                                    'd' -> set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                                    'm' -> set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                                    't' -> set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                                    'w' -> set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                                    'y' -> set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                                    'f' -> set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                                    's' -> set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                                    else -> throw IllegalStateException("Cannot have another any other char representing a day of the week")
                                }
                                alarmCreationViewModel.alarmBusInfo.value!!.time
                                    .subtract(beforeTime).also {
                                        if (it == null) throw IllegalStateException("An error occured trying to calculate alarm times to go off")
                                        set(Calendar.HOUR_OF_DAY, it.hour)
                                        set(Calendar.MINUTE, it.min)
                                        set(Calendar.SECOND, it.sec)
                                    }
                                (requireActivity() as MainActivity).setAlarm(requireContext(), this)
                            }

                        }
                    }
                    alarmCreationViewModel.createAlarm{
                        setFragmentResult("NEW_ALARM",
                            bundleOf(Pair("ON_ACCEPT", true))
                        )
                        dismiss()
                    }
                }
            }

        })
        childFragmentManager.beginTransaction()
            .add(R.id.createAlarmsDialogBottomNav, bottomNavBar)
            .commit()
    }
}