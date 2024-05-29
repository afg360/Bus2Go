package dev.mainhq.bus2go.fragments

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.viewmodel.AlarmCreationViewModel


private const val UNSELECTED = "UNSELECTED"
private const val SELECTED = "SELECTED"

class AlarmCreationDialog(private val alarmCreationViewModel : AlarmCreationViewModel) : DialogFragment(R.layout.fragment_create_alarms_dialog) {

    private lateinit var bottomNavBar: AlarmCreationDialogBottomNavBar

    private var allZero = true
    private var allUnselected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Schedules_Dark)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //alarmCreationViewModel = ViewModelProvider(this)[AlarmCreationViewModel::class.java]

        view.findViewById<MaterialTextView>(R.id.textViewChooseStop).setOnClickListener { _ ->
            val alarmCreationChooseBusDialog = AlarmCreationChooseBusDialog()
            //TODO DEPRECATED FOR DATA EXCHANGE alarmCreationDialog.setTargetFragment(this@Alarms, 0)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.add(alarmCreationChooseBusDialog, null).commit()
            setFragmentResultListener("requestKey") { requestKey, bundle ->
                if (Build.VERSION.SDK_INT < 33) {
                    alarmCreationViewModel.updateAlarmBus(bundle.getParcelable("ALARM_BUS_INFO")!!)
                } else {
                    alarmCreationViewModel.updateAlarmBus(bundle.getParcelable("", AlarmCreationChooseBusDialog.AlarmBusInfo::class.java)!!)
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
                            if (!allUnselected && !allZero) bottomNavBar.activateAcceptAlarmButton()
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
                this@AlarmCreationDialog.alarmCreationViewModel.createAlarm()
                dismiss()
            }

        })
        childFragmentManager.beginTransaction()
            .add(R.id.createAlarmsDialogBottomNav, bottomNavBar)
            .commit()
    }
}